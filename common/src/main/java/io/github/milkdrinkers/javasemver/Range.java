package io.github.milkdrinkers.javasemver;

import io.github.milkdrinkers.javasemver.exception.RangeParseException;
import io.github.milkdrinkers.javasemver.internal.RangeAlgebra;
import io.github.milkdrinkers.javasemver.internal.RangeParsing;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

/**
 * A semver range e.g. {@code >=1.2.3 <2.0.0 || 3.x}, consisting of one or more comparator sets
 * joined by {@code ||} (OR), where each comparator set is one or more {@link Constraint}'s that
 * must all match (AND).
 *
 * @apiNote Implements {@link Predicate} so a range can be used directly as a filter e.g. {@code versions.stream().filter(Range.parse(">=1.2.3"))}.
 */
public final class Range implements Predicate<Version> {
    private final List<List<Constraint>> comparatorSets;

    private Range(List<List<Constraint>> comparatorSets) {
        final List<List<Constraint>> copy = new ArrayList<>();
        for (List<Constraint> set : comparatorSets)
            copy.add(Collections.unmodifiableList(new ArrayList<>(set)));

        this.comparatorSets = Collections.unmodifiableList(copy);
    }

    /**
     * Parses a range string e.g. {@code >=1.2.3 <2.0.0 || 3.x}.
     *
     * @param input the range string to parse
     * @return the parsed range
     * @throws RangeParseException thrown if the string could not be parsed into a range
     */
    public static @NotNull Range parse(String input) throws RangeParseException {
        return new Range(RangeParsing.parse(input));
    }

    /**
     * Parses a range string leniently e.g. {@code >=v1.2.3 <2.0.0 || 3.x}, tolerating a leading {@code v}/{@code =} and leading zeros in each constraints version part.
     *
     * @param input the range string to parse
     * @return the parsed range
     * @throws RangeParseException thrown if the string could not be parsed into a range
     * @apiNote Only the version part of primitive comparators (e.g. {@code >=v1.2.3}) is parsed leniently. X-ranges, tilde/caret and hyphen ranges already tolerate leading zeros.
     */
    public static @NotNull Range parseLoose(String input) throws RangeParseException {
        return new Range(RangeParsing.parse(input, true));
    }

    /**
     * Parses a range string e.g. {@code >=1.2.3 <2.0.0 || 3.x}.
     *
     * @param input the range string to parse
     * @return the parsed range wrapped in an optional, or an empty optional if parsing failed
     */
    public static @NotNull Optional<Range> parseOptional(String input) {
        try {
            return Optional.of(parse(input));
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    /**
     * Checks whether a range string is valid.
     *
     * @param input the range string to check
     * @return whether the string could be parsed into a range
     * @apiNote Uses {@link Range#parseOptional(String)} internally
     */
    public static boolean isValid(String input) {
        return parseOptional(input).isPresent();
    }

    /**
     * Finds the highest version in a collection that satisfies a range.
     *
     * @param versions the candidate versions
     * @param range    the range to satisfy
     * @return the highest satisfying version by natural order, or an empty optional if none satisfy the range
     */
    public static @NotNull Optional<Version> maxSatisfying(Collection<Version> versions, Range range) {
        return versions.stream()
            .filter(range::contains)
            .max(Comparator.naturalOrder());
    }

    /**
     * Finds the highest version in a collection that satisfies a range.
     *
     * @param versions the candidate versions
     * @param range    the range string to parse and satisfy
     * @return the highest satisfying version by natural order, or an empty optional if none satisfy the range
     * @throws RangeParseException thrown if the range string could not be parsed
     * @apiNote Delegates to {@link #parse(String)} to parse the range
     */
    public static @NotNull Optional<Version> maxSatisfying(Collection<Version> versions, String range) {
        return maxSatisfying(versions, parse(range));
    }

    /**
     * Finds the lowest version in a collection that satisfies a range.
     *
     * @param versions the candidate versions
     * @param range    the range to satisfy
     * @return the lowest satisfying version by natural order, or an empty optional if none satisfy the range
     */
    public static @NotNull Optional<Version> minSatisfying(Collection<Version> versions, Range range) {
        return versions.stream()
            .filter(range::contains)
            .min(Comparator.naturalOrder());
    }

    /**
     * Finds the lowest version in a collection that satisfies a range.
     *
     * @param versions the candidate versions
     * @param range    the range string to parse and satisfy
     * @return the lowest satisfying version by natural order, or an empty optional if none satisfy the range
     * @throws RangeParseException thrown if the range string could not be parsed
     * @apiNote Delegates to {@link #parse(String)} to parse the range
     */
    public static @NotNull Optional<Version> minSatisfying(Collection<Version> versions, String range) {
        return minSatisfying(versions, parse(range));
    }

    /**
     * Checks whether a version satisfies this range.
     * <p>
     * A version with a prerelease tag may only satisfy a comparator set if that set contains at least one constraint whose version also
     * has a prerelease tag and shares the same {@code major.minor.patch} as the candidate. This
     * keeps prerelease versions from matching ranges that don't mention that prerelease line.
     *
     * @param v the version to check
     * @return true if the version satisfies at least one comparator set (OR), where a set is satisfied if the version satisfies all of its constraints (AND) and, when the version has a prerelease, the set opts into that prerelease line
     */
    public boolean contains(@NotNull Version v) {
        for (List<Constraint> comparatorSet : comparatorSets) {
            if (!setAllowsPrerelease(comparatorSet, v))
                continue;

            boolean allMatch = true;

            for (Constraint constraint : comparatorSet) {
                if (!constraint.contains(v)) {
                    allMatch = false;
                    break;
                }
            }

            if (allMatch)
                return true;
        }

        return false;
    }

    /**
     * Checks whether a comparator set is allowed to match a prerelease candidate.
     * <p>
     * Stable candidates are always allowed through. A prerelease candidate is only allowed
     * through a set if the set contains a constraint whose version also carries a prerelease tag
     * and shares the candidates exact {@code major.minor.patch}.
     *
     * @param comparatorSet the comparator set (AND-group) being evaluated
     * @param candidate     the version being tested against the range
     * @return true if the set may be used to evaluate the candidate
     */
    private static boolean setAllowsPrerelease(@NotNull List<Constraint> comparatorSet, @NotNull Version candidate) {
        if (!candidate.hasPreRelease())
            return true;

        for (Constraint constraint : comparatorSet) {
            final Version constraintVersion = constraint.getVersion();

            if (constraintVersion == null || !constraintVersion.hasPreRelease())
                continue;

            if (constraintVersion.getMajor() == candidate.getMajor()
                && constraintVersion.getMinor() == candidate.getMinor()
                && constraintVersion.getPatch() == candidate.getPatch())
                return true;
        }

        return false;
    }

    /**
     * Predicate implementation, delegating to {@link #contains(Version)}.
     *
     * @param v the version to test
     * @return true if the version satisfies this range
     */
    @Override
    public boolean test(Version v) {
        return contains(v);
    }

    /**
     * Gets the comparator sets making up this range.
     *
     * @return the unmodifiable list of comparator sets, joined by OR, each containing constraints joined by AND
     */
    public @NotNull List<List<Constraint>> getComparatorSets() {
        return comparatorSets;
    }

    /**
     * Checks whether this range matches any version.
     *
     * @return true if any comparator set is empty or contains the wildcard constraint
     */
    public boolean isAny() {
        for (List<Constraint> comparatorSet : comparatorSets) {
            if (comparatorSet.isEmpty())
                return true;

            for (Constraint constraint : comparatorSet) {
                if (constraint.isAny())
                    return true;
            }
        }

        return false;
    }

    /**
     * Finds the lowest version this range can possibly satisfy.
     *
     * @return the lowest satisfying version by natural order, or an empty optional if this range admits no version at all (e.g. every comparator set is contradictory)
     * @apiNote Delegates to {@link RangeAlgebra#minVersion(Range)}
     */
    public @NotNull Optional<Version> minVersion() {
        return RangeAlgebra.minVersion(this);
    }

    /**
     * Checks whether this range and another range intersect, e.g. share at least one version they could both possibly match.
     *
     * @param other the other range
     * @return true if there exists a version that could satisfy both this range and {@code other}
     * @apiNote Delegates to {@link RangeAlgebra#intersects(Range, Range)}
     */
    public boolean intersects(@NotNull Range other) {
        return RangeAlgebra.intersects(this, other);
    }

    /**
     * Checks whether this range is a subset of another range, e.g. every version this range could possibly match is also matched by {@code dom}.
     *
     * @param dom the candidate superset (dominating) range
     * @return true if every version this range could match, {@code dom} could also match
     * @apiNote Delegates to {@link RangeAlgebra#isSubsetOf(Range, Range)}
     */
    public boolean isSubsetOf(@NotNull Range dom) {
        return RangeAlgebra.isSubsetOf(this, dom);
    }

    /**
     * Finds the shortest range that has the same membership as {@code range} over a supplied set of versions.
     * <p>
     * The versions are grouped into maximal contiguous runs of versions that satisfy
     * {@code range}, and each run is collapsed to its tightest primitive form ({@code >=first
     * <=last}, or a bare version for a single-element run), joined by {@code ||}. This preserves
     * membership for every version in {@code versions}, though it isn't guaranteed to be the
     * globally shortest possible string.
     *
     * @param versions the versions whose membership in {@code range} must be preserved
     * @param range    the range to simplify
     * @return a range equivalent to {@code range} for every version in {@code versions}. If none of the supplied versions satisfy {@code range}, a range that matches nothing
     * @apiNote Delegates to {@link RangeAlgebra#simplify(Collection, Range)}
     */
    public static @NotNull Range simplify(@NotNull Collection<Version> versions, @NotNull Range range) {
        return RangeAlgebra.simplify(versions, range);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Range)) return false;
        Range range = (Range) o;
        return Objects.equals(comparatorSets, range.comparatorSets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(comparatorSets);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < comparatorSets.size(); i++) {
            if (i > 0)
                sb.append(" || ");

            final List<Constraint> comparatorSet = comparatorSets.get(i);
            for (int j = 0; j < comparatorSet.size(); j++) {
                if (j > 0)
                    sb.append(" ");

                sb.append(comparatorSet.get(j));
            }
        }

        return sb.toString();
    }
}
