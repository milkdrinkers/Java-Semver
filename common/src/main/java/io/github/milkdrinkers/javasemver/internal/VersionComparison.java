package io.github.milkdrinkers.javasemver.internal;

import io.github.milkdrinkers.javasemver.Version;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Precedence and build aware comparison logic used by {@link Version}.
 */
@ApiStatus.Internal
public final class VersionComparison {
    private VersionComparison() {
    }

    /**
     * Compares two versions by Semantic Versioning precedence: major, minor, patch and then
     * pre release identifiers. Build metadata plays no role in this comparison.
     *
     * @param a the first version
     * @param b the second version
     * @return {@code -1}, {@code 0} or {@code 1} as {@code a} is less than, equal to, or greater than {@code b}
     */
    public static int comparePrecedence(@NotNull Version a, @NotNull Version b) {
        int result = signum(a.getMajor() - b.getMajor());
        if (result != 0)
            return result;

        result = signum(a.getMinor() - b.getMinor());
        if (result != 0)
            return result;

        result = signum(a.getPatch() - b.getPatch());
        if (result != 0)
            return result;

        return comparePreRelease(a.getPreReleaseIdentifiers(), b.getPreReleaseIdentifiers());
    }

    /**
     * Compares two versions by Semantic Versioning precedence and then by build metadata as a tiebreak.
     * A version with build metadata is considered greater than an otherwise equal version without any. This is the opposite of how missing pre release identifiers are treated.
     *
     * @param a the first version
     * @param b the second version
     * @return {@code -1}, {@code 0} or {@code 1} as {@code a} is less than, equal to, or greater than {@code b}
     */
    public static int compareBuild(@NotNull Version a, @NotNull Version b) {
        final int p = comparePrecedence(a, b);
        if (p != 0)
            return p;

        return compareBuildIdentifiers(splitDot(a.getBuildMetadata()), splitDot(b.getBuildMetadata()));
    }

    /**
     * Compares pre release identifier lists.
     */
    private static int comparePreRelease(@NotNull List<String> currentIdentifiers, @NotNull List<String> otherIdentifiers) {
        if (currentIdentifiers.isEmpty()) {
            return otherIdentifiers.isEmpty() ? 0 : 1;
        } else if (otherIdentifiers.isEmpty()) {
            return -1;
        }

        final int minLength = Math.min(currentIdentifiers.size(), otherIdentifiers.size());

        for (int i = 0; i < minLength; i++) {
            final int comparison = compareIdentifier(currentIdentifiers.get(i), otherIdentifiers.get(i));
            if (comparison != 0)
                return comparison;
        }

        return signum(currentIdentifiers.size() - otherIdentifiers.size());
    }

    /**
     * Compares build metadata identifier lists. Unlike pre release identifiers, an empty (missing) build metadata identifier list sorts lower, not higher.
     */
    private static int compareBuildIdentifiers(@NotNull List<String> currentIdentifiers, @NotNull List<String> otherIdentifiers) {
        final int minLength = Math.min(currentIdentifiers.size(), otherIdentifiers.size());

        for (int i = 0; i < minLength; i++) {
            final int comparison = compareIdentifier(currentIdentifiers.get(i), otherIdentifiers.get(i));
            if (comparison != 0)
                return comparison;
        }

        return signum(currentIdentifiers.size() - otherIdentifiers.size());
    }

    /**
     * Compares a single pair of dot separated identifiers using the numeric vs alphanumeric rule.
     * Numeric identifiers are compared numerically and are always lower than alphanumeric ones, which are compared lexically.
     */
    private static int compareIdentifier(@NotNull String currentId, @NotNull String otherId) {
        final boolean isCurrentNumeric = isNumeric(currentId);
        final boolean isOtherNumeric = isNumeric(otherId);

        if (isCurrentNumeric && !isOtherNumeric) {
            return -1;
        } else if (!isCurrentNumeric && isOtherNumeric) {
            return 1;
        } else if (isCurrentNumeric) {
            return signum(Long.parseLong(currentId) - Long.parseLong(otherId));
        } else {
            return signum(currentId.compareTo(otherId));
        }
    }

    /**
     * Checks if an identifier is numeric, e.g. consists only of digits. Leading zeros are allowed. Strictly parsed pre release identifiers can never carry them, but build metadata identifiers may.
     *
     * @param identifier The identifier to check
     * @return true if the identifier is numeric, false otherwise
     */
    private static boolean isNumeric(@Nullable String identifier) {
        return identifier != null && !identifier.isEmpty() && identifier.matches("\\d+");
    }

    /**
     * Splits a dot separated identifier string into its component identifiers, treating an empty string as an empty list rather than a single empty identifier.
     */
    private static @NotNull List<String> splitDot(@NotNull String value) {
        final List<String> ids = new ArrayList<>();
        for (String s : value.split("\\.")) {
            if (!s.isEmpty())
                ids.add(s);
        }
        return ids;
    }

    /**
     * Clamps an arbitrary integer difference to {@code -1}, {@code 0} or {@code 1}.
     */
    private static int signum(long value) {
        return Long.compare(value, 0L);
    }
}
