package io.github.milkdrinkers.javasemver.internal;

import io.github.milkdrinkers.javasemver.Constraint;
import io.github.milkdrinkers.javasemver.Version;
import io.github.milkdrinkers.javasemver.enums.Operator;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Reduces one AND-set of {@link Constraint}s (e.g. the constraints parsed from a single OR-part of
 * a range, such as {@code >=1.2.0 <2.0.0}) to a single interval over the version total order.
 *
 * <p>The interval is represented as a lower and an upper bound, each of which is either a
 * {@link Version} paired with an inclusive/exclusive flag, or {@code null} meaning unbounded
 * ({@code -} for the lower bound, {@code +} for the upper bound). This normalized form makes it
 * cheap to test two AND-sets for overlap ({@link #overlaps(Interval, Interval)}) without having to
 * reason about each constraint individually.</p>
 *
 * <p>All version comparisons are done via {@link VersionComparison#comparePrecedence(Version, Version)}, e.g. build metadata is ignored, matching the rest of the range matching logic.</p>
 */
@ApiStatus.Internal
public final class Interval {
    private final Version lower;
    private final boolean lowerInclusive;
    private final Version upper;
    private final boolean upperInclusive;

    private Interval(@Nullable Version lower, boolean lowerInclusive, @Nullable Version upper, boolean upperInclusive) {
        this.lower = lower;
        this.lowerInclusive = lowerInclusive;
        this.upper = upper;
        this.upperInclusive = upperInclusive;
    }

    /**
     * Combines an AND-set of constraints into the single interval that is their intersection.
     *
     * <p>Each constraint contributes a half bounded (or, for {@code EQ}, fully bounded) interval:</p>
     * <ul>
     *     <li>{@code any} &rarr; {@code (-, +)}, contributes nothing.</li>
     *     <li>{@code EQ v} &rarr; lower = {@code v} inclusive, upper = {@code v} inclusive.</li>
     *     <li>{@code GT v} &rarr; lower = {@code v} exclusive.</li>
     *     <li>{@code GTE v} &rarr; lower = {@code v} inclusive.</li>
     *     <li>{@code LT v} &rarr; upper = {@code v} exclusive.</li>
     *     <li>{@code LTE v} &rarr; upper = {@code v} inclusive.</li>
     * </ul>
     *
     * <p>Each contribution is intersected into a running result that starts fully unbounded
     * ({@code (-, +)}):</p>
     * <ul>
     *     <li>A candidate lower bound replaces the running lower bound only if it is strictly
     *     <em>tighter</em>, e.g. strictly greater (a {@code -} running lower bound is always beaten
     *     by any real version). If the candidate's version equals the running lower bound's version,
     *     the version is kept but the bound becomes inclusive only if <em>both</em> contributions
     *     were inclusive, the exclusive (tighter) side wins.</li>
     *     <li>Uppers are handled symmetrically: a candidate replaces the running upper bound only if
     *     it is strictly <em>lesser</em>, and ties keep the exclusive side.</li>
     * </ul>
     *
     * @param andSet the AND-ed constraints to combine e.g. the constraints of one OR-part of a range
     * @return the interval that is the intersection of all of {@code andSet}'s constraints
     */
    public static @NotNull Interval of(@NotNull List<Constraint> andSet) {
        Bound lower = new Bound(null, false);
        Bound upper = new Bound(null, false);

        for (final Constraint constraint : andSet) {
            if (constraint.isAny())
                continue;

            final Operator operator = constraint.getOperator();
            final Version version = constraint.getVersion();

            switch (operator) {
                case GT:
                    lower = lower.tightenLower(version, false);
                    break;
                case GTE:
                    lower = lower.tightenLower(version, true);
                    break;
                case LT:
                    upper = upper.tightenUpper(version, false);
                    break;
                case LTE:
                    upper = upper.tightenUpper(version, true);
                    break;
                case EQ:
                    lower = lower.tightenLower(version, true);
                    upper = upper.tightenUpper(version, true);
                    break;
                default:
                    break;
            }
        }

        return new Interval(lower.version, lower.inclusive, upper.version, upper.inclusive);
    }

    /**
     * An immutable (version, inclusive) pair representing one side of a running interval bound,
     * with {@code version == null} meaning unbounded. Kept as its own tiny type so that tightening
     * a bound is a single atomic "replace with the new (version, inclusive) pair or keep the old
     * one" decision, never a partial update where the version and inclusivity are computed
     * against inconsistent snapshots of each other.
     */
    private static final class Bound {
        private final Version version;
        private final boolean inclusive;

        private Bound(@Nullable Version version, boolean inclusive) {
            this.version = version;
            this.inclusive = inclusive;
        }

        /**
         * Intersects this lower bound with a candidate {@code (candidate, candidateInclusive)} lower bound, keeping whichever is strictly tighter (greater), or, on a tie, the exclusive one.
         */
        private Bound tightenLower(@NotNull Version candidate, boolean candidateInclusive) {
            if (this.version == null)
                return new Bound(candidate, candidateInclusive);

            final int c = VersionComparison.comparePrecedence(candidate, this.version);
            if (c > 0)
                return new Bound(candidate, candidateInclusive);
            if (c < 0)
                return this;

            return new Bound(this.version, this.inclusive && candidateInclusive);
        }

        /**
         * Intersects this upper bound with a candidate {@code (candidate, candidateInclusive)} upper bound, keeping whichever is strictly tighter (lesser), or, on a tie, the exclusive one. Symmetric to {@link #tightenLower}.
         */
        private Bound tightenUpper(@NotNull Version candidate, boolean candidateInclusive) {
            if (this.version == null)
                return new Bound(candidate, candidateInclusive);

            final int c = VersionComparison.comparePrecedence(candidate, this.version);
            if (c < 0)
                return new Bound(candidate, candidateInclusive);
            if (c > 0)
                return this;

            return new Bound(this.version, this.inclusive && candidateInclusive);
        }
    }

    /**
     * Gets the lower bound of this interval.
     *
     * @return the lower bound, or {@code null} meaning unbounded ({@code -})
     */
    public @Nullable Version getLower() {
        return lower;
    }

    /**
     * Checks whether the lower bound is inclusive.
     *
     * @return true if the lower bound (when non null) is included in the interval
     */
    public boolean isLowerInclusive() {
        return lowerInclusive;
    }

    /**
     * Gets the upper bound of this interval.
     *
     * @return the upper bound, or {@code null} meaning unbounded ({@code +})
     */
    public @Nullable Version getUpper() {
        return upper;
    }

    /**
     * Checks whether the upper bound is inclusive.
     *
     * @return true if the upper bound (when non null) is included in the interval
     */
    public boolean isUpperInclusive() {
        return upperInclusive;
    }

    /**
     * Checks whether this interval contains no versions at all.
     *
     * <p>When both bounds are present: empty if {@code lower > upper}, or if {@code lower == upper}
     * and either end is exclusive (a single point interval {@code [v, v]} is non empty only when
     * both ends include {@code v}). If either bound is unbounded ({@code null}), the interval can
     * never be empty on account of that side.</p>
     *
     * @return true if this interval is empty
     */
    public boolean isEmpty() {
        if (lower == null || upper == null)
            return false;

        final int c = VersionComparison.comparePrecedence(lower, upper);
        if (c > 0)
            return true;
        if (c == 0)
            return !(lowerInclusive && upperInclusive);

        return false;
    }

    /**
     * Checks whether two intervals overlap, e.g. share at least one version.
     *
     * <p>An empty interval never overlaps anything. Otherwise, two non empty intervals overlap iff
     * each one's lower endpoint is at or before the other's upper endpoint, where endpoints are only
     * allowed to "touch" (equal versions) if both sides that meet there actually include that point.</p>
     *
     * @param a the first interval
     * @param b the second interval
     * @return true if {@code a} and {@code b} share at least one version
     */
    public static boolean overlaps(@NotNull Interval a, @NotNull Interval b) {
        if (a.isEmpty() || b.isEmpty())
            return false;

        return lowerLessOrEqualUpper(a.lower, a.lowerInclusive, b.upper, b.upperInclusive)
            && lowerLessOrEqualUpper(b.lower, b.lowerInclusive, a.upper, a.upperInclusive);
    }

    /**
     * Checks whether a lower endpoint is less than or equal to an upper endpoint, where the endpoints are only allowed to be equal ("touch") if both sides include that point.
     *
     * @param lowerVersion   the lower endpoint's version, or {@code null} for {@code -}
     * @param lowerInclusive whether the lower endpoint includes {@code lowerVersion}
     * @param upperVersion   the upper endpoint's version, or {@code null} for {@code +}
     * @param upperInclusive whether the upper endpoint includes {@code upperVersion}
     * @return true if the lower endpoint is at or before the upper endpoint
     */
    private static boolean lowerLessOrEqualUpper(@Nullable Version lowerVersion, boolean lowerInclusive, @Nullable Version upperVersion, boolean upperInclusive) {
        if (lowerVersion == null || upperVersion == null)
            return true;

        final int c = VersionComparison.comparePrecedence(lowerVersion, upperVersion);
        if (c < 0)
            return true;
        if (c > 0)
            return false;

        return lowerInclusive && upperInclusive;
    }
}
