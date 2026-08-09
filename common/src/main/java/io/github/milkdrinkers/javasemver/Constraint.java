package io.github.milkdrinkers.javasemver;

import io.github.milkdrinkers.javasemver.enums.Operator;
import io.github.milkdrinkers.javasemver.exception.RangeParseException;
import io.github.milkdrinkers.javasemver.internal.VersionComparison;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * A single semver comparator e.g. {@code >=1.2.3} consisting of an {@link Operator} and a
 * {@link Version}. Also supports wildcard comparator ({@code *} or {@code x}) which matches any version.
 *
 * @apiNote Implements {@link Predicate} so a constraint can be used directly as a filter e.g. {@code versions.stream().filter(Constraint.parse(">=1.2.3"))}.
 */
public class Constraint implements Predicate<Version> {
    private final Operator operator;
    private final Version version;
    private final boolean any;

    private Constraint(Operator operator, Version version, boolean any) {
        this.operator = operator;
        this.version = version;
        this.any = any;
    }

    /**
     * Creates a constraint from an explicit operator and version e.g. {@code new Constraint(Operator.GTE, someVersion)} to represent {@code >=1.2.3}.
     *
     * @param operator the comparison operator
     * @param version  the version to compare against
     * @apiNote Intended for internal range-expansion logic (e.g. X-ranges, tilde/caret ranges)
     * that needs to build constraints directly from already-parsed components rather than
     * from a comparator string.
     */
    public Constraint(@NotNull Operator operator, @NotNull Version version) {
        this(Objects.requireNonNull(operator, "operator must not be null"), Objects.requireNonNull(version, "version must not be null"), false);
    }

    /**
     * Parses a single comparator string e.g. {@code >=1.2.3}, {@code 1.2.3} or {@code *}.
     *
     * @param input the comparator string to parse
     * @return the parsed constraint
     * @throws RangeParseException thrown if the string could not be parsed into a constraint
     */
    public static @NotNull Constraint parse(String input) throws RangeParseException {
        return parse(input, false);
    }

    /**
     * Parses a single comparator string e.g. {@code >=1.2.3}, {@code 1.2.3} or {@code *}.
     *
     * @param input the comparator string to parse
     * @param loose whether the version part should be parsed leniently via {@link Version#parseLoose(String)} (tolerating a leading {@code v}/{@code =} and leading zeros) rather than strictly
     * @return the parsed constraint
     * @throws RangeParseException thrown if the string could not be parsed into a constraint
     */
    public static @NotNull Constraint parse(String input, boolean loose) throws RangeParseException {
        if (input == null)
            throw new RangeParseException("Could not parse range constraint from: null");

        final String trimmed = input.trim();

        if (trimmed.isEmpty() || trimmed.equals("*") || trimmed.equalsIgnoreCase("x"))
            return any();

        Operator operator = null;
        String versionPart = trimmed;

        if (trimmed.length() >= 2) {
            final Operator twoChar = Operator.fromSymbol(trimmed.substring(0, 2));
            if (twoChar != null) {
                operator = twoChar;
                versionPart = trimmed.substring(2);
            }
        }

        if (operator == null) {
            final Operator oneChar = Operator.fromSymbol(trimmed.substring(0, 1));
            if (oneChar != null) {
                operator = oneChar;
                versionPart = trimmed.substring(1);
            }
        }

        if (operator == null)
            operator = Operator.EQ;

        try {
            final Version parsedVersion = loose ? Version.parseLoose(versionPart.trim()) : Version.parse(versionPart.trim());
            return new Constraint(operator, parsedVersion, false);
        } catch (RuntimeException e) {
            throw new RangeParseException("Could not parse range constraint from: " + input, e);
        }
    }

    /**
     * Creates a wildcard constraint, which matches any version.
     *
     * @return a wildcard constraint
     */
    public static @NotNull Constraint any() {
        return new Constraint(null, null, true);
    }

    /**
     * Checks whether a version satisfies this constraint.
     *
     * @param v the version to check
     * @return true if the version satisfies this constraint
     */
    public boolean contains(@NotNull Version v) {
        if (any)
            return true;

        final int comparison = VersionComparison.comparePrecedence(v, version);

        switch (operator) {
            case GT:
                return comparison > 0;
            case GTE:
                return comparison >= 0;
            case LT:
                return comparison < 0;
            case LTE:
                return comparison <= 0;
            case EQ:
                return comparison == 0;
            default:
                return false;
        }
    }

    /**
     * Predicate implementation, delegating to {@link #contains(Version)}.
     *
     * @param v the version to test
     * @return true if the version satisfies this constraint
     */
    @Override
    public boolean test(Version v) {
        return contains(v);
    }

    /**
     * Gets the operator of this constraint.
     *
     * @return the operator, or {@code null} if this is the wildcard constraint
     */
    public @Nullable Operator getOperator() {
        return operator;
    }

    /**
     * Gets the version of this constraint.
     *
     * @return the version, or {@code null} if this is the wildcard constraint
     */
    public @Nullable Version getVersion() {
        return version;
    }

    /**
     * Checks whether this is the wildcard constraint.
     *
     * @return true if this constraint matches any version
     */
    public boolean isAny() {
        return any;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Constraint)) return false;
        Constraint that = (Constraint) o;
        return any == that.any
            && operator == that.operator
            && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operator, version, any);
    }

    @Override
    public String toString() {
        if (any)
            return "*";

        return (operator == Operator.EQ ? "" : operator.getSymbol()) + version;
    }
}
