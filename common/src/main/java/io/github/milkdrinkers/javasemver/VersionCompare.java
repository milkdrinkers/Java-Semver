package io.github.milkdrinkers.javasemver;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Contains all methods used for comparing two {@link Version} objects according to the Semantic Versioning 2.0 specification.
 */
public abstract class VersionCompare {
    /**
     * Takes a result from Java compare methods and returns the equivalent {@link VersionCheckResult}.
     *
     * @param result any integer
     * @return {@link VersionCheckResult#EQUAL} if 0, {@link VersionCheckResult#OLDER} if less than 0, {@link VersionCheckResult#NEWER} if greater than 0.
     */
    @ApiStatus.Internal
    private static VersionCheckResult result(long result) {
        if (result == 0D)
            return VersionCheckResult.EQUAL;

        return result < 0D ? VersionCheckResult.OLDER : VersionCheckResult.NEWER;
    }

    /**
     * Compare major, minor, patch and then pre-release data of two versions.
     *
     * @param current the current version to compare with
     * @param other the other version to compare against
     * @return the version check result
     * @apiNote Follows <a href="https://semver.org/">Semver spec</a> such that this is always true: {@code 1.0.0-alpha < 1.0.0-alpha.1 < 1.0.0-alpha.beta < 1.0.0-beta < 1.0.0-beta.2 < 1.0.0-beta.11 < 1.0.0-rc.1 < 1.0.0 < 2.0.0 < 2.1.0 < 2.1.1}.
     */
    public static @NotNull VersionCheckResult compare(@NotNull Version current, @NotNull Version other) {
        final long majorChange = current.getMajor() - other.getMajor();
        final long minorChange = current.getMinor() - other.getMinor();
        final long patchChange = current.getPatch() - other.getPatch();

        // If there are any changes in versioning we know whether the version is newer or older
        if (majorChange != 0)
            return result(majorChange);

        if (minorChange != 0)
            return result(minorChange);

        if (patchChange != 0)
            return result(patchChange);

        // No change in major, minor or patch, check pre-release identifiers
        return comparePreRelease(current, other);
    }

    /**
     * Compare pre-release data of two versions.
     *
     * @param current the current version to compare with
     * @param other the other version to compare against
     * @return the version check result
     * @apiNote Follows <a href="https://semver.org/#spec-item-11">Semver spec</a> such that this is always true: {@code 1.0.0-alpha < 1.0.0-alpha.1 < 1.0.0-alpha.beta < 1.0.0-beta < 1.0.0-beta.2 < 1.0.0-beta.11 < 1.0.0-rc.1 < 1.0.0}.
     */
    public static @NotNull VersionCheckResult comparePreRelease(@NotNull Version current, @NotNull Version other) {
        final String[] currentIds = current.getPreReleaseIdentifiers();
        final String[] otherIds = other.getPreReleaseIdentifiers();
        final int currentSize = currentIds.length;
        final int otherSize = otherIds.length;

        // Both have no pre-release identifiers
        if (currentSize == 0 && otherSize == 0)
            return VersionCheckResult.EQUAL;

        // One has no pre-release identifiers (Pre-releases have lower precedence than normal releases)
        if (currentSize == 0 || otherSize == 0)
            return currentSize == 0 ? VersionCheckResult.NEWER : VersionCheckResult.OLDER;


        // Iterate through identifiers and compare
        VersionCheckResult compareResult = VersionCheckResult.EQUAL;

        for (int i = 0; i < Math.min(currentSize, otherSize); i++) {
            compareResult = compareIdentifier(currentIds[i], otherIds[i]);

            // Iterate until identifiers are not equal
            if (!compareResult.equals(VersionCheckResult.EQUAL))
                break;
        }

        // Handle if the identifiers are equal (The one with more identifiers takes precedence)
        if (compareResult.equals(VersionCheckResult.EQUAL))
            return result(currentSize - otherSize);

        return compareResult;
    }

    /**
     * Compare pre-release identifiers against each-other.
     *
     * @param currentId the current identifier to compare with
     * @param otherId the other identifier to compare against
     * @return the version check result
     * @apiNote Follows <a href="https://semver.org/#spec-item-11">Semver spec</a> such that this is always true: {@code 1.0.0-alpha < 1.0.0-alpha.1 < 1.0.0-alpha.beta < 1.0.0-beta < 1.0.0-beta.2 < 1.0.0-beta.11 < 1.0.0-rc.1 < 1.0.0}.
     */
    public static @NotNull VersionCheckResult compareIdentifier(final @NotNull String currentId, final @NotNull String otherId) {
        // If both identifiers are valid numbers for semver then compare
        if (isNumber(currentId) && isNumber(otherId)) {
            final Long currentLong = Long.valueOf(currentId);
            final Long otherLong = Long.valueOf(otherId);

            return result(currentLong.compareTo(otherLong));
        }

        // Compare identifiers lexicographically
        return result(currentId.compareTo(otherId));
    }

    /**
     * Check if a pre-release identifier is a valid number for semver comparison.
     *
     * @param identifier the identifier to check
     * @return boolean
     */
    @ApiStatus.Internal
    private static boolean isNumber(final @NotNull String identifier) {
        // Numbers with leading zeroes are invalid in semver
        if (identifier.startsWith("0"))
            return false;

        return identifier.chars().allMatch(Character::isDigit);
    }

    /**
     * Check if two versions are the same.
     *
     * @param current the current version
     * @param other the other version
     * @return if currentVersion is same as otherVersion
     */
    @SuppressWarnings("unused")
    public static boolean isEqual(@NotNull Version current, @NotNull Version other) {
        return compare(current, other).equals(VersionCheckResult.EQUAL);
    }

    /**
     * Check if one version is newer.
     *
     * @param current the current version
     * @param other the other version
     * @return if currentVersion is newer
     */
    @SuppressWarnings("unused")
    public static boolean isNewer(@NotNull Version current, @NotNull Version other) {
        return compare(current, other).equals(VersionCheckResult.NEWER);
    }

    /**
     * Check if one version is older.
     *
     * @param current the current version
     * @param other the other version
     * @return if currentVersion is older
     */
    @SuppressWarnings("unused")
    public static boolean isOlder(@NotNull Version current, @NotNull Version other) {
        return compare(current, other).equals(VersionCheckResult.OLDER);
    }

    /**
     * Check if one version is newer or same.
     *
     * @param current the current version
     * @param other the other version
     * @return if currentVersion is newer or equal to
     */
    @SuppressWarnings("unused")
    public static boolean isNewerOrEqual(@NotNull Version current, @NotNull Version other) {
        return isNewer(current, other) || isEqual(current, other);
    }

    /**
     * Check if one version is older or same.
     *
     * @param current the current version
     * @param other the other version
     * @return if currentVersion is older or equal to
     */
    @SuppressWarnings("unused")
    public static boolean isOlderOrEqual(@NotNull Version current, @NotNull Version other) {
        return isOlder(current, other) || isEqual(current, other);
    }
}
