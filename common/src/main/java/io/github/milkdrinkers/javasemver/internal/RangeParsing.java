package io.github.milkdrinkers.javasemver.internal;

import io.github.milkdrinkers.javasemver.Constraint;
import io.github.milkdrinkers.javasemver.Version;
import io.github.milkdrinkers.javasemver.enums.Operator;
import io.github.milkdrinkers.javasemver.exception.RangeParseException;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Range parsing logic used by {@code Range}.
 *
 * @apiNote Handles the OR-split ({@code ||}), hyphen ranges (e.g. {@code 1.2.3 - 2.3.4}),
 * space separated primitive comparators (plus the {@code *}/empty wildcard), X-ranges/partial
 * versions (e.g. {@code 1.x}, {@code 1.2.*}, {@code 1}, {@code 1.2}), and tilde/caret ranges (e.g. {@code ~1.2.3}, {@code ^1.2.3}) within each OR-part.
 */
@ApiStatus.Internal
public final class RangeParsing {
    /**
     * Matches a bare (no leading operator) X-range or partial version token e.g. {@code 1},
     * {@code 1.2}, {@code 1.2.3}, {@code 1.x}, {@code 1.2.*}. Each of the up to three dot separated
     * components is either a non negative integer or a wildcard marker ({@code x}, {@code X} or {@code *}), trailing components may be omitted entirely.
     */
    private static final Pattern X_RANGE_PATTERN = Pattern.compile("^([0-9]+|[xX*])(\\.([0-9]+|[xX*])(\\.([0-9]+|[xX*]))?)?$");

    /**
     * Matches an entire hyphen range OR-part e.g. {@code 1.2.3 - 2.3.4} a token, then a hyphen
     * surrounded by whitespace, then a token. Must be checked before the OR-part is split on
     * whitespace, since the from/to tokens themselves contain no spaces but are separated by them.
     */
    private static final Pattern HYPHEN_RANGE_PATTERN = Pattern.compile("^\\s*(\\S+)\\s+-\\s+(\\S+)\\s*$");

    private RangeParsing() {
    }

    /**
     * Parses a range string into a list of comparator sets, where the outer list represents an OR relationship between sets and the inner list represents an AND relationship between the constraints of a set.
     *
     * @param rangeStr the range string to parse
     * @return the parsed comparator sets
     * @throws RangeParseException thrown if the range string could not be parsed
     */
    public static List<List<Constraint>> parse(String rangeStr) throws RangeParseException {
        return parse(rangeStr, false);
    }

    /**
     * Parses a range string into a list of comparator sets, where the outer list represents an
     * OR relationship between sets and the inner list represents an AND relationship between the constraints of a set.
     *
     * @param rangeStr the range string to parse
     * @param loose    whether primitive comparator versions (e.g. {@code >=v1.2.0}) should be parsed leniently via {@link io.github.milkdrinkers.javasemver.Version#parseLoose(String)} rather than strictly
     * @return the parsed comparator sets
     * @throws RangeParseException thrown if the range string could not be parsed
     */
    public static List<List<Constraint>> parse(String rangeStr, boolean loose) throws RangeParseException {
        if (rangeStr == null)
            throw new RangeParseException("Could not parse range from: null");

        final String trimmed = rangeStr.trim();

        if (trimmed.isEmpty() || trimmed.equals("*")) {
            final List<Constraint> anySet = new ArrayList<>();
            anySet.add(Constraint.any());
            final List<List<Constraint>> comparatorSets = new ArrayList<>();
            comparatorSets.add(anySet);
            return comparatorSets;
        }

        final List<List<Constraint>> comparatorSets = new ArrayList<>();
        for (String orPart : trimmed.split("\\|\\|")) {
            if (orPart.trim().isEmpty())
                continue;

            final List<Constraint> comparatorSet = parseComparatorSet(orPart, loose);
            if (!comparatorSet.isEmpty())
                comparatorSets.add(comparatorSet);
        }

        if (comparatorSets.isEmpty())
            throw new RangeParseException("Could not parse range from: " + rangeStr);

        return comparatorSets;
    }

    /**
     * Parses a single OR-part (a space separated series of comparators that must all match, e.g. an AND relationship) into a comparator set.
     *
     * <p>A hyphen range (e.g. {@code 1.2.3 - 2.3.4}) is detected first, before the OR-part is
     * split on whitespace, since it is itself whitespace separated (unlike every other supported
     * token). Anything that is not a hyphen range falls through to the original token by token handling.</p>
     */
    private static List<Constraint> parseComparatorSet(String part, boolean loose) {
        final Matcher hyphenMatcher = HYPHEN_RANGE_PATTERN.matcher(part);
        if (hyphenMatcher.matches())
            return expandHyphenRange(part, hyphenMatcher.group(1), hyphenMatcher.group(2));

        final List<Constraint> constraints = new ArrayList<>();

        for (String token : part.trim().split("\\s+")) {
            if (token.isEmpty())
                continue;

            if (token.charAt(0) == '~') {
                constraints.addAll(expandTildeRange(token));
                continue;
            }

            if (token.charAt(0) == '^') {
                constraints.addAll(expandCaretRange(token));
                continue;
            }

            if (X_RANGE_PATTERN.matcher(token).matches()) {
                constraints.addAll(expandXRange(token));
                continue;
            }

            constraints.add(Constraint.parse(token, loose));
        }

        return constraints;
    }

    /**
     * Expands a hyphen range (e.g. {@code 1.2.3 - 2.3.4}), already split into its {@code from} and {@code to} tokens by {@link #HYPHEN_RANGE_PATTERN}, into the equivalent {@code >=lower <=upper} (or {@code <upper} when {@code to} is partial) constraint pair.
     *
     * <ul>
     *     <li>{@code from} is partial &rarr; missing components are filled with zero for the lower
     *     bound e.g. {@code 1.2 - ...} &rarr; {@code >=1.2.0 ...}, {@code 1 - ...} &rarr;
     *     {@code >=1.0.0 ...}.</li>
     *     <li>{@code to} is fully specified &rarr; {@code <=} that version (prerelease preserved).</li>
     *     <li>{@code to} is partial &rarr; the next component up is bumped and the bound becomes
     *     exclusive e.g. {@code ... - 2.3} &rarr; {@code ... <2.4.0-0}, {@code ... - 2} &rarr;
     *     {@code ... <3.0.0-0}.</li>
     * </ul>
     */
    private static List<Constraint> expandHyphenRange(String part, String fromToken, String toToken) {
        final PartialVersion from = parsePartial(part, fromToken);
        final PartialVersion to = parsePartial(part, toToken);

        final List<Constraint> constraints = new ArrayList<>();

        if (!from.any)
            constraints.add(new Constraint(Operator.GTE, from.toLowerVersion()));

        if (to.any)
            return constraints;

        if (to.specified >= 3) {
            constraints.add(new Constraint(Operator.LTE, to.toLowerVersion()));
            return constraints;
        }

        final Version upper = to.specified == 2
            ? Version.of(to.major, to.minor + 1L, 0L, "0")
            : Version.of(to.major + 1L, 0L, 0L, "0");
        constraints.add(new Constraint(Operator.LT, upper));

        return constraints;
    }

    /**
     * Expands a bare X-range/partial version token (already validated against {@link #X_RANGE_PATTERN}) into the equivalent list of AND-ed constraints.
     *
     * <ul>
     *     <li>major is a wildcard (e.g. {@code x}, {@code *}) &rarr; a single {@link Constraint#any()}</li>
     *     <li>minor is a wildcard or absent (e.g. {@code 1}, {@code 1.x}) &rarr; {@code >=M.0.0 <(M+1).0.0}</li>
     *     <li>patch is a wildcard or absent (e.g. {@code 1.2}, {@code 1.2.x}) &rarr; {@code >=M.m.0 <M.(m+1).0}</li>
     *     <li>fully specified (e.g. {@code 1.2.3}) &rarr; a single {@code =M.m.p} equality</li>
     * </ul>
     */
    private static List<Constraint> expandXRange(String token) {
        final String[] parts = token.split("\\.", -1);
        final String majorPart = parts[0];
        final String minorPart = parts.length > 1 ? parts[1] : null;
        final String patchPart = parts.length > 2 ? parts[2] : null;

        final List<Constraint> constraints = new ArrayList<>();

        if (isWildcardComponent(majorPart)) {
            constraints.add(Constraint.any());
            return constraints;
        }

        try {
            final long major = Long.parseLong(majorPart);

            if (isWildcardComponent(minorPart)) {
                constraints.add(new Constraint(Operator.GTE, Version.of(major, 0L, 0L)));
                constraints.add(new Constraint(Operator.LT, Version.of(major + 1L, 0L, 0L, "0")));
                return constraints;
            }

            final long minor = Long.parseLong(minorPart);

            if (isWildcardComponent(patchPart)) {
                constraints.add(new Constraint(Operator.GTE, Version.of(major, minor, 0L)));
                constraints.add(new Constraint(Operator.LT, Version.of(major, minor + 1L, 0L, "0")));
                return constraints;
            }

            final long patch = Long.parseLong(patchPart);
            constraints.add(new Constraint(Operator.EQ, Version.of(major, minor, patch)));
            return constraints;
        } catch (NumberFormatException e) {
            throw new RangeParseException("Could not parse range from: " + token, e);
        }
    }

    /**
     * Expands a tilde range token (e.g. {@code ~1.2.3}), which already carries its leading {@code ~}, into the equivalent {@code >=lower <upper} constraint pair.
     *
     * <p>Allows patch level changes if minor is specified, otherwise minor level changes:</p>
     * <ul>
     *     <li>{@code ~M.m.p} &rarr; {@code >=M.m.p <M.(m+1).0-0}</li>
     *     <li>{@code ~M.m} &rarr; {@code >=M.m.0 <M.(m+1).0-0}</li>
     *     <li>{@code ~M} &rarr; {@code >=M.0.0 <(M+1).0.0-0}</li>
     * </ul>
     *
     * <p>A prerelease on the version part (e.g. {@code ~1.2.3-beta}) is preserved on the lower bound but never affects the upper bound.</p>
     */
    private static List<Constraint> expandTildeRange(String token) {
        final PartialVersion partial = parsePartial(token, token.substring(1));

        final List<Constraint> constraints = new ArrayList<>();
        if (partial.any) {
            constraints.add(Constraint.any());
            return constraints;
        }

        constraints.add(new Constraint(Operator.GTE, partial.toLowerVersion()));

        final Version upper = partial.specified >= 2
            ? Version.of(partial.major, partial.minor + 1L, 0L, "0")
            : Version.of(partial.major + 1L, 0L, 0L, "0");
        constraints.add(new Constraint(Operator.LT, upper));

        return constraints;
    }

    /**
     * Expands a caret range token (e.g. {@code ^1.2.3}), which already carries its leading {@code ^}, into the equivalent {@code >=lower <upper} constraint pair.
     *
     * <p>Allows changes that do not modify the left most non zero of the <em>specified</em>
     * components e.g. {@code ^1.2.3} &rarr; {@code >=1.2.3 <2.0.0-0}, {@code ^0.2.3} &rarr;
     * {@code >=0.2.3 <0.3.0-0}, {@code ^0.0.3} &rarr; {@code >=0.0.3 <0.0.4-0}. If every specified
     * component is zero (e.g. {@code ^0.0}), the least significant specified component is bumped
     * instead (e.g. {@code ^0.0} &rarr; {@code <0.1.0-0}, {@code ^0} &rarr; {@code <1.0.0-0}).</p>
     *
     * <p>A prerelease on the version part is preserved on the lower bound but never affects the upper bound.</p>
     */
    private static List<Constraint> expandCaretRange(String token) {
        final PartialVersion partial = parsePartial(token, token.substring(1));

        final List<Constraint> constraints = new ArrayList<>();
        if (partial.any) {
            constraints.add(Constraint.any());
            return constraints;
        }

        constraints.add(new Constraint(Operator.GTE, partial.toLowerVersion()));
        constraints.add(new Constraint(Operator.LT, caretUpperBound(partial)));

        return constraints;
    }

    /**
     * Computes the caret upper bound: the left most non zero of the specified major/minor/patch
     * components is incremented and everything to its right is zeroed. If every specified
     * component is zero, the least significant specified component is incremented instead.
     */
    private static Version caretUpperBound(PartialVersion partial) {
        if (partial.major > 0L)
            return Version.of(partial.major + 1L, 0L, 0L, "0");

        if (partial.specified >= 2 && partial.minor > 0L)
            return Version.of(0L, partial.minor + 1L, 0L, "0");

        if (partial.specified >= 3 && partial.patch > 0L)
            return Version.of(0L, 0L, partial.patch + 1L, "0");

        switch (partial.specified) {
            case 1:
                return Version.of(1L, 0L, 0L, "0");
            case 2:
                return Version.of(0L, 1L, 0L, "0");
            default:
                return Version.of(0L, 0L, 1L, "0");
        }
    }

    /**
     * Parses the version part following a stripped {@code ~}/{@code ^} prefix into a
     * {@link PartialVersion}, tolerating X-range wildcards/absent trailing components (e.g.
     * {@code 1}, {@code 1.2}, {@code 1.x}) and a full version carrying a prerelease (e.g. {@code 1.2.3-beta}).
     *
     * @param token     the original token (including its {@code ~}/{@code ^} prefix), used only for error messages
     * @param remainder the version part with the {@code ~}/{@code ^} prefix already stripped
     */
    private static PartialVersion parsePartial(String token, String remainder) {
        try {
            // build metadata never affects, strip here
            final int plusIndex = remainder.indexOf('+');
            final String noBuild = plusIndex >= 0 ? remainder.substring(0, plusIndex) : remainder;

            if (noBuild.contains("-")) {
                final Version v = Version.parse(noBuild);
                return new PartialVersion(v.getMajor(), v.getMinor(), v.getPatch(), 3, v.getPreRelease(), false);
            }

            if (!X_RANGE_PATTERN.matcher(noBuild).matches())
                throw new RangeParseException("Could not parse range from: " + token);

            final String[] parts = noBuild.split("\\.", -1);
            final String majorPart = parts[0];
            final String minorPart = parts.length > 1 ? parts[1] : null;
            final String patchPart = parts.length > 2 ? parts[2] : null;

            if (isWildcardComponent(majorPart))
                return new PartialVersion(0L, 0L, 0L, 0, "", true);

            final long major = Long.parseLong(majorPart);

            if (isWildcardComponent(minorPart))
                return new PartialVersion(major, 0L, 0L, 1, "", false);

            final long minor = Long.parseLong(minorPart);

            if (isWildcardComponent(patchPart))
                return new PartialVersion(major, minor, 0L, 2, "", false);

            final long patch = Long.parseLong(patchPart);
            return new PartialVersion(major, minor, patch, 3, "", false);
        } catch (RangeParseException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RangeParseException("Could not parse range from: " + token, e);
        }
    }

    /**
     * Checks whether an X-range component is a wildcard, e.g. {@code x}, {@code X}, {@code *}, or absent (null).
     */
    private static boolean isWildcardComponent(String component) {
        return component == null || component.equals("x") || component.equals("X") || component.equals("*");
    }

    /**
     * The decomposed major/minor/patch/prerelease of a tilde/caret version part, along with how
     * many of the leading components were actually specified (as opposed to wildcard/absent) and
     * whether the whole thing was a bare wildcard (e.g. {@code ~x}, {@code ^*}).
     */
    private static final class PartialVersion {
        private final long major;
        private final long minor;
        private final long patch;
        private final int specified;
        private final String preRelease;
        private final boolean any;

        private PartialVersion(long major, long minor, long patch, int specified, String preRelease, boolean any) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
            this.specified = specified;
            this.preRelease = preRelease;
            this.any = any;
        }

        private Version toLowerVersion() {
            return preRelease.isEmpty() ? Version.of(major, minor, patch) : Version.of(major, minor, patch, preRelease);
        }
    }
}
