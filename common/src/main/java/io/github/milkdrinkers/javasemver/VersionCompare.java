package io.github.milkdrinkers.javasemver;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * @param other   the other version to compare against
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
     * @param other   the other version to compare against
     * @return the version check result
     * @apiNote Follows <a href="https://semver.org/#spec-item-11">Semver spec</a> such that this is always true: {@code 1.0.0-alpha < 1.0.0-alpha.1 < 1.0.0-alpha.beta < 1.0.0-beta < 1.0.0-beta.2 < 1.0.0-beta.11 < 1.0.0-rc.1 < 1.0.0}.
     */
    @SuppressWarnings("ConstantValue")
    public static @NotNull VersionCheckResult comparePreRelease(@NotNull Version current, @NotNull Version other) {
        final String[] currentIdentifiers = current.getPreReleaseIdentifiers();
        final String[] otherIdentifiers = other.getPreReleaseIdentifiers();

        // If one array is empty and the other isn't, the version with a pre-release has lower precedence
        if (currentIdentifiers == null || currentIdentifiers.length == 0) {
            if (otherIdentifiers == null || otherIdentifiers.length == 0) {
                return VersionCheckResult.EQUAL;
            }
            return VersionCheckResult.NEWER; // No pre-release has higher precedence
        } else if (otherIdentifiers == null || otherIdentifiers.length == 0) {
            return VersionCheckResult.OLDER; // identifiers1 has pre-release, identifiers2 doesn't
        }

        // Compare each identifier in sequence
        final int minLength = Math.min(currentIdentifiers.length, otherIdentifiers.length);

        for (int i = 0; i < minLength; i++) {
            final String currentId = currentIdentifiers[i];
            final String otherId = otherIdentifiers[i];

            // Check if both identifiers are numeric
            final boolean isCurrentNumeric = isNumeric(currentId);
            final boolean isOtherNumeric = isNumeric(otherId);

            // Rule 3: Numeric identifiers have lower precedence than non-numeric identifiers
            if (isCurrentNumeric && !isOtherNumeric) {
                return VersionCheckResult.OLDER;
            } else if (!isCurrentNumeric && isOtherNumeric) {
                return VersionCheckResult.NEWER;
            } else if (isCurrentNumeric && isOtherNumeric) {
                // Rule 1: Numeric comparison for numeric identifiers
                int num1 = Integer.parseInt(currentId);
                int num2 = Integer.parseInt(otherId);

                if (num1 < num2) {
                    return VersionCheckResult.OLDER;
                } else if (num1 > num2) {
                    return VersionCheckResult.NEWER;
                }
                // Equal, continue iter to next id
            } else {
                // Rule 2: Lexical comparison for non-numeric identifiers
                int comparison = currentId.compareTo(otherId);

                if (comparison < 0) {
                    return VersionCheckResult.OLDER;
                } else if (comparison > 0) {
                    return VersionCheckResult.NEWER;
                }
                // Equal, continue iter to next id
            }
        }

        // Rule 4: If all identifiers so far are equal, the longer array has higher precedence
        if (currentIdentifiers.length < otherIdentifiers.length) {
            return VersionCheckResult.OLDER;
        } else if (currentIdentifiers.length > otherIdentifiers.length) {
            return VersionCheckResult.NEWER;
        }

        // All identifiers are equal
        return VersionCheckResult.EQUAL;
    }

    /**
     * Checks if a string is a numeric identifier according to SemVer rules.
     * A numeric identifier consists of only digits with no leading zeros (except for "0" itself).
     *
     * @param identifier The identifier to check
     * @return true if the identifier is numeric, false otherwise
     * @apiNote Follows <a href="https://semver.org/#spec-item-11">Semver spec</a> such that this is always true: {@code 1.0.0-alpha < 1.0.0-alpha.1 < 1.0.0-alpha.beta < 1.0.0-beta < 1.0.0-beta.2 < 1.0.0-beta.11 < 1.0.0-rc.1 < 1.0.0}.
     */
    @SuppressWarnings("RedundantIfStatement")
    private static boolean isNumeric(@Nullable String identifier) {
        if (identifier == null || identifier.isEmpty())
            return false;

        // Check if the string consists of only digits
        if (!identifier.matches("\\d+"))
            return false;

        // Check for leading zeros (but "0" by itself is fine)
        if (identifier.length() > 1 && identifier.startsWith("0"))
            return false;

        return true;
    }

    /**
     * Check if two versions are the same.
     *
     * @param current the current version
     * @param other   the other version
     * @return true if current version is same as other version
     */
    @SuppressWarnings("unused")
    public static boolean isEqual(@NotNull Version current, @NotNull Version other) {
        return compare(current, other).equals(VersionCheckResult.EQUAL);
    }

    /**
     * Check if one version is newer.
     *
     * @param current the current version
     * @param other   the other version
     * @return true if current version is newer than other version
     */
    @SuppressWarnings("unused")
    public static boolean isNewer(@NotNull Version current, @NotNull Version other) {
        return compare(current, other).equals(VersionCheckResult.NEWER);
    }

    /**
     * Check if one version is older.
     *
     * @param current the current version
     * @param other   the other version
     * @return true if current version is older than other version
     */
    @SuppressWarnings("unused")
    public static boolean isOlder(@NotNull Version current, @NotNull Version other) {
        return compare(current, other).equals(VersionCheckResult.OLDER);
    }

    /**
     * Check if one version is newer or same.
     *
     * @param current the current version
     * @param other   the other version
     * @return true if current version is newer or equal to other version
     */
    @SuppressWarnings("unused")
    public static boolean isNewerOrEqual(@NotNull Version current, @NotNull Version other) {
        return isNewer(current, other) || isEqual(current, other);
    }

    /**
     * Check if one version is older or same.
     *
     * @param current the current version
     * @param other   the other version
     * @return true if current version is older or equal to other version
     */
    @SuppressWarnings("unused")
    public static boolean isOlderOrEqual(@NotNull Version current, @NotNull Version other) {
        return isOlder(current, other) || isEqual(current, other);
    }
}
