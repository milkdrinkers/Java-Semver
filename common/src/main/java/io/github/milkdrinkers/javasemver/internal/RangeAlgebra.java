package io.github.milkdrinkers.javasemver.internal;

import io.github.milkdrinkers.javasemver.Constraint;
import io.github.milkdrinkers.javasemver.Range;
import io.github.milkdrinkers.javasemver.Version;
import io.github.milkdrinkers.javasemver.enums.Operator;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Algebraic operations over {@link Range} built on top of the per comparator set {@link Interval} representation e.g. computing the lowest version a range can possibly admit.
 */
@ApiStatus.Internal
public final class RangeAlgebra {
    private RangeAlgebra() {
    }

    /**
     * Finds the lowest version a range can possibly satisfy.
     * <p>
     * Each comparator set (an AND-group joined by OR at the {@link Range} level) is reduced to an
     * {@link Interval}. Empty intervals contribute nothing. For every other interval, the interval's
     * own minimum is computed, {@code 0.0.0} for an unbounded ({@code -}) lower bound, the
     * lower bound itself when inclusive, or the next version after it when exclusive, and kept
     * only if the range as a whole actually admits it, since the interval's minimum can still be
     * excluded by another AND-ed upper bound or by the range's prerelease rule. The least of the
     * surviving candidates is the range's minimum.
     *
     * @param range the range to inspect
     * @return the lowest satisfying version by natural order, or an empty optional if the range admits no version at all
     */
    public static @NotNull Optional<Version> minVersion(@NotNull Range range) {
        final List<Version> candidates = new ArrayList<>();

        for (final List<Constraint> comparatorSet : range.getComparatorSets()) {
            final Interval interval = Interval.of(comparatorSet);
            if (interval.isEmpty())
                continue;

            final Version candidate = intervalMinimum(interval);
            if (range.contains(candidate))
                candidates.add(candidate);
        }

        return candidates.stream().min(Comparator.naturalOrder());
    }

    /**
     * Checks whether two ranges intersect, e.g. share at least one version they could both possibly match.
     * <p>
     * Each range is reduced to its list of non empty {@link Interval}s, one per comparator set
     * (an AND-group joined by OR at the {@link Range} level). Comparator sets that reduce to an
     * empty interval (contradictory AND-ed constraints) are dropped, since they can never
     * contribute a version. The two ranges intersect iff some interval of {@code a} overlaps some interval of {@code b}.
     *
     * @param a the first range
     * @param b the second range
     * @return true if the ranges share at least one version they could both match
     */
    public static boolean intersects(@NotNull Range a, @NotNull Range b) {
        final List<Interval> intervalsA = nonEmptyIntervals(a);
        final List<Interval> intervalsB = nonEmptyIntervals(b);

        for (final Interval intervalA : intervalsA) {
            for (final Interval intervalB : intervalsB) {
                if (Interval.overlaps(intervalA, intervalB))
                    return true;
            }
        }

        return false;
    }

    /**
     * An unbounded lower bound comparator, standing in for the {@code *} wildcard.
     */
    private static final Constraint GTE_ZERO = new Constraint(Operator.GTE, Version.of(0L, 0L, 0L));

    /**
     * Checks whether a version's precedence is greater than everything a range could possibly match.
     *
     * @param v     the version to check
     * @param range the range to check against
     * @return true if {@code v} is above every version {@code range} could match
     */
    public static boolean isAbove(@NotNull Version v, @NotNull Range range) {
        return outside(v, range, true);
    }

    /**
     * Checks whether a version's precedence is less than everything a range could possibly match.
     *
     * @param v     the version to check
     * @param range the range to check against
     * @return true if {@code v} is below every version {@code range} could match
     */
    public static boolean isBelow(@NotNull Version v, @NotNull Range range) {
        return outside(v, range, false);
    }

    /**
     * Whether {@code version} lies entirely above ({@code above == true}, e.g. {@code gtr}) or below ({@code above == false}, e.g. {@code ltr})
     * every version {@code range} could match. For each OR-ed comparator set the highest and
     * lowest semver comparators are found, then the version is judged against those endpoints and their operators.
     *
     * @param version the version to check
     * @param range   the range to check against
     * @param above   {@code true} for {@code gtr}, {@code false} for {@code ltr}
     * @return true if {@code version} is entirely outside {@code range} in the requested direction
     */
    private static boolean outside(@NotNull Version version, @NotNull Range range, boolean above) {
        if (range.contains(version))
            return false;

        for (final List<Constraint> set : range.getComparatorSets()) {
            Constraint high = null;
            Constraint low = null;
            for (final Constraint raw : set) {
                final Constraint c = raw.isAny() ? GTE_ZERO : raw;
                if (high == null) {
                    high = c;
                    low = c;
                } else if (edgeGreater(c.getVersion(), high.getVersion(), above)) {
                    high = c;
                } else if (edgeLess(c.getVersion(), low.getVersion(), above)) {
                    low = c;
                }
            }

            if (high == null)
                continue;

            final Operator hop = high.getOperator();
            final Operator lop = low.getOperator();

            if (above) {
                if (hop == Operator.GT || hop == Operator.GTE)
                    return false;
                if ((lop == Operator.EQ || lop == Operator.GT) && lte(version, low.getVersion()))
                    return false;
                else if (lop == Operator.GTE && lt(version, low.getVersion()))
                    return false;
            } else {
                if (hop == Operator.LT || hop == Operator.LTE)
                    return false;
                if ((lop == Operator.EQ || lop == Operator.LT) && gte(version, low.getVersion()))
                    return false;
                else if (lop == Operator.LTE && gt(version, low.getVersion()))
                    return false;
            }
        }

        return true;
    }

    /**
     * greater than for {@code gtr}, less than for {@code ltr}.
     */
    private static boolean edgeGreater(Version a, Version b, boolean above) {
        return above ? gt(a, b) : lt(a, b);
    }

    /**
     * less than for {@code gtr}, greater than for {@code ltr}.
     */
    private static boolean edgeLess(Version a, Version b, boolean above) {
        return above ? lt(a, b) : gt(a, b);
    }

    private static boolean gt(Version a, Version b) {
        return VersionComparison.comparePrecedence(a, b) > 0;
    }

    private static boolean lt(Version a, Version b) {
        return VersionComparison.comparePrecedence(a, b) < 0;
    }

    private static boolean gte(Version a, Version b) {
        return VersionComparison.comparePrecedence(a, b) >= 0;
    }

    private static boolean lte(Version a, Version b) {
        return VersionComparison.comparePrecedence(a, b) <= 0;
    }

    /**
     * Checks whether every version {@code sub} could possibly match is also matched by {@code dom}, e.g. whether {@code sub} is a subset of {@code dom}.
     * <p>
     * {@code *} is the universal set, so anything is a subset of it, and it is a subset of nothing
     * else (unless the other side is also {@code *}). Otherwise both ranges are reduced to their
     * non empty {@link Interval}s (one per comparator set, dropping contradictory AND-groups). A
     * range that reduces to no intervals at all matches nothing, and the empty set is trivially a
     * subset of anything. Otherwise {@code sub} is a subset of {@code dom} iff every one of
     * {@code sub}'s intervals is wholly contained within a single one of {@code dom}'s intervals
     * a sub interval that straddles two dom intervals (with a gap of unmatched versions between them) is not contained in either.
     *
     * @param sub the candidate subset range
     * @param dom the candidate superset (dominating) range
     * @return true if every version {@code sub} could match, {@code dom} could also match
     */
    public static boolean isSubsetOf(@NotNull Range sub, @NotNull Range dom) {
        if (dom.isAny())
            return true;

        if (sub.isAny())
            return false;

        for (final List<Constraint> subSet : sub.getComparatorSets()) {
            final Interval subInterval = Interval.of(subSet);
            if (subInterval.isEmpty())
                continue;

            boolean contained = false;
            for (final List<Constraint> domSet : dom.getComparatorSets()) {
                final Interval domInterval = Interval.of(domSet);
                if (domInterval.isEmpty())
                    continue;

                if (contains(domInterval, subInterval) && prereleaseCovered(subInterval, domSet)) {
                    contained = true;
                    break;
                }
            }

            if (!contained)
                return false;
        }

        return true;
    }

    /**
     * If the sub range's lower or upper bound
     * carries a prerelease, the matched dominator set must contain a comparator that also has a
     * prerelease and shares that bound's major.minor.patch tuple, otherwise the sub range
     * admits prereleases in that tuple the dominator does not. The exclusive {@code <X.Y.Z-0}
     * upper bound produced by caret/tilde ranges is exempt, since it is equivalent to a plain
     * {@code <X.Y.Z}.
     *
     * @param subInterval the sub set's interval
     * @param domSet      the dominator comparator set being tested for containment
     * @return true if the dominator satisfies the prerelease requirement of the sub range's bounds
     */
    private static boolean prereleaseCovered(@NotNull Interval subInterval, @NotNull List<Constraint> domSet) {
        final Version lower = subInterval.getLower();
        if (lower != null && lower.hasPreRelease() && !domHasPrereleaseAtTuple(domSet, lower))
            return false;

        final Version upper = subInterval.getUpper();
        return upper == null || !upper.hasPreRelease() || isExclusiveZeroPrerelease(subInterval) || domHasPrereleaseAtTuple(domSet, upper);
    }

    /**
     * Whether the interval's upper bound is an exclusive {@code <X.Y.Z-0} bound (a single {@code 0} prerelease identifier).
     */
    private static boolean isExclusiveZeroPrerelease(@NotNull Interval subInterval) {
        final Version upper = subInterval.getUpper();
        return upper != null
            && !subInterval.isUpperInclusive()
            && upper.getPreReleaseIdentifiers().size() == 1
            && upper.getPreReleaseIdentifiers().get(0).equals("0");
    }

    /**
     * Whether a comparator set contains a comparator whose version has a prerelease and shares the given version's major.minor.patch tuple.
     */
    private static boolean domHasPrereleaseAtTuple(@NotNull List<Constraint> domSet, @NotNull Version tuple) {
        for (final Constraint c : domSet) {
            final Version cv = c.getVersion();
            if (cv != null && cv.hasPreRelease()
                && cv.getMajor() == tuple.getMajor()
                && cv.getMinor() == tuple.getMinor()
                && cv.getPatch() == tuple.getPatch())
                return true;
        }

        return false;
    }

    /**
     * Checks whether a (contiguous) interval {@code subI} is wholly contained within another
     * (contiguous) interval {@code domI}, e.g. {@code domI}'s lower bound is at or before
     * {@code subI}'s lower bound, and {@code domI}'s upper bound is at or after {@code subI}'s
     * upper bound, both honoring inclusivity at the boundary.
     *
     * @param domI the (candidate) containing interval
     * @param subI the (candidate) contained interval
     * @return true if every version {@code subI} admits is also admitted by {@code domI}
     */
    private static boolean contains(@NotNull Interval domI, @NotNull Interval subI) {
        final boolean lowerCovered;
        if (domI.getLower() == null) {
            lowerCovered = true;
        } else if (subI.getLower() == null) {
            lowerCovered = false;
        } else {
            final int c = VersionComparison.comparePrecedence(domI.getLower(), subI.getLower());
            if (c < 0)
                lowerCovered = true;
            else if (c > 0)
                lowerCovered = false;
            else
                lowerCovered = domI.isLowerInclusive() || !subI.isLowerInclusive();
        }

        final boolean upperCovered;
        if (domI.getUpper() == null) {
            upperCovered = true;
        } else if (subI.getUpper() == null) {
            upperCovered = false;
        } else {
            final int c = VersionComparison.comparePrecedence(subI.getUpper(), domI.getUpper());
            if (c < 0)
                upperCovered = true;
            else if (c > 0)
                upperCovered = false;
            else
                upperCovered = domI.isUpperInclusive() || !subI.isUpperInclusive();
        }

        return lowerCovered && upperCovered;
    }

    /**
     * Reduces a range's comparator sets to the list of their non empty {@link Interval}s.
     *
     * @param range the range to inspect
     * @return the non empty intervals of {@code range}, one per comparator set that isn't contradictory
     */
    private static @NotNull List<Interval> nonEmptyIntervals(@NotNull Range range) {
        final List<Interval> intervals = new ArrayList<>();

        for (final List<Constraint> comparatorSet : range.getComparatorSets()) {
            final Interval interval = Interval.of(comparatorSet);
            if (!interval.isEmpty())
                intervals.add(interval);
        }

        return intervals;
    }

    /**
     * Finds the shortest range that has the same membership as {@code range} over a supplied set of versions.
     * <p>
     * The versions are sorted ascending and walked in order, grouping them into maximal
     * contiguous runs of versions that satisfy {@code range}, a run breaks the moment a
     * version fails {@link Range#contains(Version)}. Each run of satisfying versions is then
     * collapsed to its tightest primitive form: a single satisfying version becomes that bare
     * version, and a run spanning several becomes {@code >=first <=last}. The resulting forms are
     * OR-ed together. This does not attempt to find the globally shortest string it only
     * guarantees membership over the versions supplied is preserved.
     *
     * @param versions the versions whose membership in {@code range} must be preserved
     * @param range    the range to simplify
     * @return a range equivalent to {@code range} for every version in {@code versions}, if none of the supplied versions satisfy {@code range}, a range that matches nothing
     */
    public static @NotNull Range simplify(@NotNull Collection<Version> versions, @NotNull Range range) {
        final List<Version> sorted = new ArrayList<>(versions);
        Collections.sort(sorted);

        final List<String> runs = new ArrayList<>();
        Version runFirst = null;
        Version runLast = null;

        for (final Version v : sorted) {
            if (range.contains(v)) {
                if (runFirst == null)
                    runFirst = v;

                runLast = v;
            } else if (runFirst != null) {
                runs.add(formatRun(runFirst, runLast));
                runFirst = null;
                runLast = null;
            }
        }

        if (runFirst != null)
            runs.add(formatRun(runFirst, runLast));

        if (runs.isEmpty())
            return Range.parse("<0.0.0-0");

        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < runs.size(); i++) {
            if (i > 0)
                sb.append(" || ");

            sb.append(runs.get(i));
        }

        return Range.parse(sb.toString());
    }

    /**
     * Formats a single contiguous run of satisfying versions as its tightest primitive range form.
     *
     * @param first the first (lowest) version of the run
     * @param last  the last (highest) version of the run
     * @return {@code first.toString()} if the run is a single version, otherwise {@code >=first <=last}
     */
    private static @NotNull String formatRun(@NotNull Version first, @NotNull Version last) {
        if (first.equals(last))
            return first.toString();

        return ">=" + first + " <=" + last;
    }

    /**
     * Computes the lowest version an individual (non empty) interval could admit, ignoring
     * anything outside of the interval itself (e.g. not yet checked against the owning range's
     * other comparator sets or prerelease rule).
     *
     * @param interval the interval to inspect, must not be empty
     * @return the interval's own lower bound candidate
     */
    private static @NotNull Version intervalMinimum(@NotNull Interval interval) {
        final Version lower = interval.getLower();

        if (lower == null)
            return Version.of(0, 0, 0);

        if (interval.isLowerInclusive())
            return lower;

        if (lower.hasPreRelease())
            return Version.of(lower.getMajor(), lower.getMinor(), lower.getPatch(), lower.getPreRelease() + ".0");

        return Version.of(lower.getMajor(), lower.getMinor(), lower.getPatch() + 1);
    }
}
