package io.github.milkdrinkers.javasemver;

import io.github.milkdrinkers.javasemver.enums.ReleaseType;
import io.github.milkdrinkers.javasemver.exception.VersionBuildException;
import io.github.milkdrinkers.javasemver.exception.VersionParseException;
import io.github.milkdrinkers.javasemver.internal.Coercion;
import io.github.milkdrinkers.javasemver.internal.RangeAlgebra;
import io.github.milkdrinkers.javasemver.internal.VersionComparison;
import io.github.milkdrinkers.javasemver.internal.VersionParsing;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * An immutable value object representing a Semantic Version, made up of a major, minor and
 * patch component and optional pre-release and build-metadata. Implements {@link Comparable} for precedence ordering,
 * and offers parsing/coercion factories along with comparison, increment and range operations.
 */
public class Version implements Comparable<Version> {

    // Base
    private final long major;
    private final long minor;
    private final long patch;
    private final String preRelease; // pre-release data like "SNAPSHOT-1" or "RC-3"
    private final List<String> preReleaseIdentifiers; // pre-release data divided into strings by the seperator "."
    private final String meta; // build-metadata

    // Cached
    private final boolean hasPreRelease;
    private final boolean hasBuildMetadata;
    private final String version; // version consisting of only Major.Minor.Patch
    private final String versionFull; // entire version string

    private final boolean isAlpha; // whether the pre-release contains "alpha"
    private final boolean isBeta; // whether the pre-release contains "beta"
    private final boolean isDevelopment; // whether the pre-release contains "dev", "develop" or "development"
    private final boolean isReleaseCandidate; // whether the pre-release contains "rc"
    private final boolean isSnapshot; // whether the pre-release contains "snapshot"

    /**
     * Instantiates a new version object.
     *
     * @param major      the major version
     * @param minor      the minor version
     * @param patch      the patch version
     * @param preRelease the pre-release version
     * @param meta       the build-meta
     * @implSpec Only to be used internally through builders/factories
     */
    @ApiStatus.Internal
    Version(long major, long minor, long patch, String preRelease, String meta) {
        // Base
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.preRelease = preRelease;
        final List<String> ids = new ArrayList<>();
        for (String s : preRelease.split("\\.")) {
            if (!s.isEmpty()) // or empty strings may count as identifiers
                ids.add(s);
        }
        this.preReleaseIdentifiers = Collections.unmodifiableList(ids);
        this.meta = meta;

        this.hasPreRelease = !getPreRelease().isEmpty();
        this.hasBuildMetadata = !getBuildMetadata().isEmpty();
        this.version = concatenateVersionString(this.major, this.minor, this.patch);
        this.versionFull = concatenateVersionStringFull(this.major, this.minor, this.patch, this.preRelease, this.meta);

        // Cached
        this.isAlpha = preRelease.toLowerCase().contains("alpha");
        this.isBeta = preRelease.toLowerCase().contains("beta");
        this.isDevelopment = preRelease.toLowerCase().contains("dev") || preRelease.toLowerCase().contains("develop") || preRelease.toLowerCase().contains("development");
        this.isReleaseCandidate = preRelease.toLowerCase().contains("rc");
        this.isSnapshot = preRelease.toLowerCase().contains("snapshot");
    }

    /**
     * Create a Version object from a version string.
     *
     * @param version a string containing a semantic version
     * @return a version object
     * @throws VersionParseException thrown if parsing the string into a version failed
     * @apiNote Uses {@link VersionParsing#parseStrict(String)} internally
     */
    public static @NotNull Version parse(String version) throws VersionParseException {
        return VersionParsing.parseStrict(version);
    }

    /**
     * Create a Version object from a version string.
     *
     * @param version a string containing a semantic version
     * @return a version object wrapped in a optional
     * @apiNote Uses {@link VersionParsing#parseStrict(String)} internally
     */
    public static @NotNull Optional<Version> parseOptional(String version) {
        try {
            return Optional.of(VersionParsing.parseStrict(version));
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    /**
     * Create a Version object from a version string, tolerating a leading {@code v}/{@code V}, a leading {@code =}, surrounding whitespace, and leading zeros in numeric components.
     *
     * @param version a string containing a loosely formatted semantic version
     * @return a version object
     * @throws VersionParseException thrown if parsing the string into a version failed
     * @apiNote Uses {@link VersionParsing#parseLoose(String)} internally
     */
    public static @NotNull Version parseLoose(String version) throws VersionParseException {
        return VersionParsing.parseLoose(version);
    }

    /**
     * Checks whether a version string is a valid semantic version.
     *
     * @param version a string containing a semantic version
     * @return whether the string could be parsed into a version
     * @apiNote Uses {@link Version#parseOptional(String)} internally
     */
    public static boolean isValid(String version) {
        return parseOptional(version).isPresent();
    }

    /**
     * Scans an arbitrary string for the first thing that looks like a version and coerces it
     * into a Version, defaulting any missing minor/patch component to {@code 0} and discarding
     * any trailing pre-release/build-metadata data.
     *
     * @param input the string to scan
     * @return the coerced version wrapped in an optional, or an empty optional if no version like data was found
     * @apiNote Uses {@link Coercion#coerce(String)} internally
     */
    public static Optional<Version> coerce(String input) {
        return Coercion.coerce(input);
    }

    /**
     * Normalizes a loosely formatted version string into a strict semantic version string,
     * tolerating surrounding whitespace, a leading {@code =} and a leading {@code v}/{@code V}.
     *
     * @param input the string to clean
     * @return the cleaned version string wrapped in an optional, or an empty optional if the remainder was not a valid version
     * @apiNote Uses {@link Coercion#clean(String)} internally
     */
    public static Optional<String> clean(String input) {
        return Coercion.clean(input);
    }

    /**
     * Create a Version object from semantic version data.
     *
     * @param major the major version
     * @param minor the minor version
     * @param patch the patch version
     * @return the semantic version
     * @throws VersionBuildException thrown if parsing the data into a version failed
     * @apiNote Uses {@link Builder} internally
     * @see #builder()
     */
    public static @NotNull Version of(long major, long minor, long patch) throws VersionBuildException {
        return of(major, minor, patch, "");
    }

    /**
     * Create a Version object from semantic version data.
     *
     * @param major      the major version
     * @param minor      the minor version
     * @param patch      the patch version
     * @param preRelease the pre-release version
     * @return the semantic version
     * @throws VersionBuildException thrown if parsing the data into a version failed
     * @apiNote Uses {@link Builder} internally
     * @see #builder()
     */
    public static @NotNull Version of(long major, long minor, long patch, String preRelease) throws VersionBuildException {
        return of(major, minor, patch, preRelease, "");
    }

    /**
     * Create a Version object from semantic version data.
     *
     * @param major      the major version
     * @param minor      the minor version
     * @param patch      the patch version
     * @param preRelease the pre-release version
     * @param meta       the build-meta
     * @return the semantic version
     * @throws VersionBuildException thrown if parsing the data into a version failed
     * @apiNote Uses {@link Builder} internally
     * @see #builder()
     */
    public static @NotNull Version of(long major, long minor, long patch, String preRelease, String meta) throws VersionBuildException {
        return builder()
            .withMajor(major)
            .withMinor(minor)
            .withPatch(patch)
            .withPreRelease(preRelease)
            .withMeta(meta)
            .build();
    }

    /**
     * Gets major version.
     *
     * @return the major
     */
    public long getMajor() {
        return major;
    }

    /**
     * Gets the minor version.
     *
     * @return the minor
     */
    public long getMinor() {
        return minor;
    }

    /**
     * Gets patch version.
     *
     * @return the patch
     */
    public long getPatch() {
        return patch;
    }

    /**
     * Gets pre-release version.
     *
     * @return the pre release
     */
    public @NotNull String getPreRelease() {
        return preRelease;
    }

    /**
     * Gets pre-release identifiers.
     *
     * @return the unmodifiable list of identifiers
     */
    public @NotNull List<String> getPreReleaseIdentifiers() {
        return preReleaseIdentifiers;
    }

    /**
     * Gets build-metadata.
     *
     * @return the metadata
     */
    public @NotNull String getBuildMetadata() {
        return meta;
    }

    /**
     * Returns whether pre-release is empty.
     *
     * @return boolean
     */
    public boolean hasPreRelease() {
        return hasPreRelease;
    }

    /**
     * Returns whether build-metadata is empty.
     *
     * @return boolean
     */
    public boolean hasBuildMetadata() {
        return hasBuildMetadata;
    }

    /**
     * Gets the simple Semantic version string containing only: major, minor and patch.
     *
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the full Semantic version string containing: major, minor, patch, pre-release and build-metadata.
     *
     * @return the full version
     */
    public String getVersionFull() {
        return versionFull;
    }

    /**
     * Is this version an alpha build.
     *
     * @return the boolean
     * @apiNote Essentially returns whether the pre-release contains "alpha"
     */
    public boolean isAlpha() {
        return isAlpha;
    }

    /**
     * Is this version a beta build.
     *
     * @return the boolean
     * @apiNote Essentially returns whether the pre-release contains "beta"
     */
    public boolean isBeta() {
        return isBeta;
    }

    /**
     * Is this version a dev build.
     *
     * @return the boolean
     * @apiNote Essentially returns whether the pre-release contains "dev", "develop" or "development"
     */
    public boolean isDev() {
        return isDevelopment;
    }

    /**
     * Is this version a release candidate build.
     *
     * @return the boolean
     * @apiNote Essentially returns whether the pre-release contains "rc"
     */
    public boolean isRC() {
        return isReleaseCandidate;
    }

    /**
     * Is this version a snapshot build.
     *
     * @return the boolean
     * @apiNote Essentially returns whether the pre-release contains "snapshot"
     */
    public boolean isSnapshot() {
        return isSnapshot;
    }

    /**
     * Computes the next version for a given release type, using a default numeric
     * pre-release identifier base of {@code 0} where a pre-release is introduced.
     *
     * @param release the type of increment to perform
     * @return the incremented version
     * @throws VersionBuildException thrown if the resulting version could not be built
     * @apiNote Build metadata is never carried over to the result.
     * @see #increment(ReleaseType, String)
     */
    public @NotNull Version increment(@NotNull ReleaseType release) throws VersionBuildException {
        return increment(release, null);
    }

    /**
     * Computes the next version for a given release type.
     *
     * @param release    the type of increment to perform
     * @param identifier the pre-release identifier to use for {@code PRE*} release types, may be {@code null} (or empty) to fall back to a numeric identifier of {@code 0}
     * @return the incremented version
     * @throws VersionBuildException thrown if the resulting version could not be built
     * @apiNote Build metadata is never carried over to the result.
     */
    public @NotNull Version increment(@NotNull ReleaseType release, @Nullable String identifier) throws VersionBuildException {
        Objects.requireNonNull(release, "release type must not be null");

        final Builder builder = Version.builder();

        switch (release) {
            case MAJOR:
                if (hasPreRelease() && minor == 0L && patch == 0L)
                    return builder.withMajor(major).withMinor(minor).withPatch(patch).build();

                return builder.withMajor(major + 1L).withMinor(0L).withPatch(0L).build();
            case MINOR:
                if (hasPreRelease() && patch == 0L)
                    return builder.withMajor(major).withMinor(minor).withPatch(patch).build();

                return builder.withMajor(major).withMinor(minor + 1L).withPatch(0L).build();
            case PATCH:
                if (hasPreRelease())
                    return builder.withMajor(major).withMinor(minor).withPatch(patch).build();

                return builder.withMajor(major).withMinor(minor).withPatch(patch + 1L).build();
            case PREMAJOR:
                return builder.withMajor(major + 1L).withMinor(0L).withPatch(0L).withPreRelease(nextPreReleaseBase(identifier)).build();
            case PREMINOR:
                return builder.withMajor(major).withMinor(minor + 1L).withPatch(0L).withPreRelease(nextPreReleaseBase(identifier)).build();
            case PREPATCH:
                return builder.withMajor(major).withMinor(minor).withPatch(patch + 1L).withPreRelease(nextPreReleaseBase(identifier)).build();
            case PRERELEASE:
                if (!hasPreRelease())
                    return builder.withMajor(major).withMinor(minor).withPatch(patch + 1L).withPreRelease(nextPreReleaseBase(identifier)).build();

                return builder.withMajor(major).withMinor(minor).withPatch(patch).withPreRelease(nextPreReleaseIdentifier(identifier)).build();
            default:
                throw new VersionBuildException("Unsupported release type: " + release);
        }
    }

    /**
     * Shortcut for {@code increment(ReleaseType.MAJOR)}.
     *
     * @return the next major version
     * @throws VersionBuildException thrown if the resulting version could not be built
     */
    public @NotNull Version nextMajor() throws VersionBuildException {
        return increment(ReleaseType.MAJOR);
    }

    /**
     * Shortcut for {@code increment(ReleaseType.MINOR)}.
     *
     * @return the next minor version
     * @throws VersionBuildException thrown if the resulting version could not be built
     */
    public @NotNull Version nextMinor() throws VersionBuildException {
        return increment(ReleaseType.MINOR);
    }

    /**
     * Shortcut for {@code increment(ReleaseType.PATCH)}.
     *
     * @return the next patch version
     * @throws VersionBuildException thrown if the resulting version could not be built
     */
    public @NotNull Version nextPatch() throws VersionBuildException {
        return increment(ReleaseType.PATCH);
    }

    /**
     * Builds the starting pre-release string for a fresh {@code PRE*} increment.
     */
    private static String nextPreReleaseBase(String identifier) {
        return identifier == null || identifier.isEmpty() ? "0" : identifier + ".0";
    }

    /**
     * Computes the next pre-release string for a version that already has one, following the
     * {@link ReleaseType#PRERELEASE} rules. An identifier matching the current one bumps its
     * trailing numeric part, a different identifier restarts the pre-release, and no identifier
     * just bumps the trailing numeric part of whatever is already there.
     */
    private String nextPreReleaseIdentifier(String identifier) {
        final List<String> ids = getPreReleaseIdentifiers();

        if (identifier != null && !identifier.isEmpty()) {
            if (!ids.isEmpty() && ids.get(0).equals(identifier))
                return String.join(".", bumpTrailingNumericIdentifier(ids));

            return identifier + ".0";
        }

        return String.join(".", bumpTrailingNumericIdentifier(ids));
    }

    /**
     * Increments the trailing identifier if it's numeric, otherwise appends a fresh {@code 0} identifier.
     */
    private static List<String> bumpTrailingNumericIdentifier(List<String> identifiers) {
        final List<String> bumped = new ArrayList<>(identifiers);
        final String last = bumped.isEmpty() ? "" : bumped.get(bumped.size() - 1);

        if (isNumericIdentifier(last)) {
            bumped.set(bumped.size() - 1, String.valueOf(Long.parseLong(last) + 1L));
        } else {
            bumped.add("0");
        }

        return bumped;
    }

    /**
     * Checks whether a pre-release identifier consists only of digits.
     */
    private static boolean isNumericIdentifier(String identifier) {
        if (identifier.isEmpty())
            return false;

        for (int i = 0; i < identifier.length(); i++) {
            if (!Character.isDigit(identifier.charAt(i)))
                return false;
        }

        return true;
    }

    /**
     * Concatenates to a Semantic versioning string.
     */
    private static String concatenateVersionString(long major, long minor, long patch) {
        return String.format("%s.%s.%s", major, minor, patch);
    }

    /**
     * Concatenates to a full Semantic versioning string.
     */
    private static String concatenateVersionStringFull(long major, long minor, long patch, String preRelease, String meta) {
        return String.format("%s%s%s", concatenateVersionString(major, minor, patch), (preRelease.isEmpty() ? "" : "-" + preRelease), (meta.isEmpty() ? "" : "+" + meta));
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param other the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it from being compared to this object.
     */
    @Override
    public int compareTo(@NotNull Version other) {
        return VersionComparison.comparePrecedence(this, other);
    }

    /**
     * Checks whether this version has a higher precedence than the other version.
     *
     * @param other the version to compare against
     * @return true if this version is greater than the other version
     * @apiNote Ignores build metadata, matching {@link #compareTo(Version)}.
     */
    public boolean isGreaterThan(Version other) {
        return compareTo(other) > 0;
    }

    /**
     * Checks whether this version has a precedence greater than or equal to the other version.
     *
     * @param other the version to compare against
     * @return true if this version is greater than or equal to the other version
     * @apiNote Ignores build metadata, matching {@link #compareTo(Version)}.
     */
    public boolean isAtLeast(Version other) {
        return compareTo(other) >= 0;
    }

    /**
     * Checks whether this version has a lower precedence than the other version.
     *
     * @param other the version to compare against
     * @return true if this version is less than the other version
     * @apiNote Ignores build metadata, matching {@link #compareTo(Version)}.
     */
    public boolean isLessThan(Version other) {
        return compareTo(other) < 0;
    }

    /**
     * Checks whether this version has a precedence less than or equal to the other version.
     *
     * @param other the version to compare against
     * @return true if this version is less than or equal to the other version
     * @apiNote Ignores build metadata, matching {@link #compareTo(Version)}.
     */
    public boolean isAtMost(Version other) {
        return compareTo(other) <= 0;
    }

    /**
     * Checks whether this version has the same precedence as the other version.
     *
     * @param other the version to compare against
     * @return true if this version is equal in precedence to the other version
     * @apiNote Ignores build metadata, matching {@link #compareTo(Version)}. Use {@link #equals(Object)} for strict equality.
     */
    public boolean isEqualTo(Version other) {
        return compareTo(other) == 0;
    }

    /**
     * Classifies the kind of change between this version and another version.
     *
     * @param other the version to compare against
     * @return the release type describing the difference, or an empty optional if the two versions have equal precedence
     */
    public @NotNull Optional<ReleaseType> difference(@NotNull Version other) {
        final int comparison = compareTo(other);
        if (comparison == 0)
            return Optional.empty();

        final Version high = comparison > 0 ? this : other;
        final Version low = comparison > 0 ? other : this;
        final boolean highHasPre = high.hasPreRelease();
        final boolean lowHasPre = low.hasPreRelease();

        if (lowHasPre && !highHasPre) {
            // a prerelease of a bare major (X.0.0) is always a major level change like: 1.0.0-1 -> 1.0.0 / 1.1.0 / 2.0.0 are all "major"
            if (low.getMinor() == 0L && low.getPatch() == 0L)
                return Optional.of(ReleaseType.MAJOR);

            // same major.minor.patch. the prerelease was simply dropped, so the change is at the lowest non zero component of the low version
            if (low.getMajor() == high.getMajor() && low.getMinor() == high.getMinor() && low.getPatch() == high.getPatch()) {
                if (low.getMinor() != 0L && low.getPatch() == 0L)
                    return Optional.of(ReleaseType.MINOR);

                return Optional.of(ReleaseType.PATCH);
            }
        }

        // pre* prefix only applies when higher version is itself a prerelease
        final boolean pre = highHasPre;

        if (getMajor() != other.getMajor())
            return Optional.of(pre ? ReleaseType.PREMAJOR : ReleaseType.MAJOR);

        if (getMinor() != other.getMinor())
            return Optional.of(pre ? ReleaseType.PREMINOR : ReleaseType.MINOR);

        if (getPatch() != other.getPatch())
            return Optional.of(pre ? ReleaseType.PREPATCH : ReleaseType.PATCH);

        return Optional.of(ReleaseType.PRERELEASE);
    }

    /**
     * Checks whether this version satisfies a range.
     *
     * @param range the range to check against
     * @return true if this version satisfies the range
     * @apiNote Shorthand for {@code range.contains(this)}.
     */
    public boolean satisfies(@NotNull Range range) {
        return range.contains(this);
    }

    /**
     * Checks whether this version satisfies a range string.
     *
     * @param range the range string to check against
     * @return true if this version satisfies the range
     * @throws io.github.milkdrinkers.javasemver.exception.RangeParseException thrown if the range string could not be parsed
     * @apiNote Shorthand for {@code Range.parse(range).contains(this)}.
     */
    public boolean satisfies(@NotNull String range) {
        return Range.parse(range).contains(this);
    }

    /**
     * Checks whether this version's precedence is greater than everything a range could possibly match.
     *
     * @param range the range to check against
     * @return true if this version is above every version the range could match
     */
    public boolean isAbove(@NotNull Range range) {
        return RangeAlgebra.isAbove(this, range);
    }

    /**
     * Checks whether this versions precedence is less than everything a range could possibly match.
     *
     * @param range the range to check against
     * @return true if this version is below every version the range could match
     */
    public boolean isBelow(@NotNull Range range) {
        return RangeAlgebra.isBelow(this, range);
    }

    /**
     * A comparator that orders versions by Semantic Versioning precedence using build metadata as a tiebreak when precedence is otherwise equal.
     *
     * @apiNote A version with build metadata sorts as greater than an otherwise equal version without any.
     */
    public static final Comparator<Version> BUILD_AWARE = VersionComparison::compareBuild;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Version)) return false;
        Version version = (Version) o;

        return getMajor() == version.getMajor()
            && getMinor() == version.getMinor()
            && getPatch() == version.getPatch()
            && Objects.equals(getPreRelease(), version.getPreRelease())
            && Objects.equals(getBuildMetadata(), version.getBuildMetadata());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMajor(), getMinor(), getPatch(), getPreRelease(), getBuildMetadata());
    }

    @Override
    public String toString() {
        return getVersionFull();
    }

    /**
     * Creates a new {@link Builder} for constructing a {@link Version} object.
     *
     * @return the version builder
     */
    public static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * A class allowing the creation of {@link Version} objects through this builder.
     *
     * @implSpec Major, Minor and Patch are required in a valid semantic version.
     */
    public static final class Builder {
        private Long major; // The Major version
        private Long minor; // The Minor version
        private Long patch; // The Patch version
        private String preRelease; // The pre-release data like "SNAPSHOT-1" or "RC-3"
        private String meta; // The build-metadata

        /**
         * Set major version for builder.
         *
         * @param major the major
         * @return the version builder
         */
        public Builder withMajor(long major) {
            this.major = major;
            return this;
        }

        /**
         * Set minor version for builder.
         *
         * @param minor the minor
         * @return the version builder
         */
        public Builder withMinor(long minor) {
            this.minor = minor;
            return this;
        }

        /**
         * Set patch version for builder.
         *
         * @param patch the patch
         * @return the version builder
         */
        public Builder withPatch(long patch) {
            this.patch = patch;
            return this;
        }

        /**
         * Set pre-release version for builder.
         *
         * @param preRelease the pre-release
         * @return the version builder
         */
        public Builder withPreRelease(String preRelease) {
            this.preRelease = preRelease;
            return this;
        }

        /**
         * Set build-metadata version for builder.
         *
         * @param meta the build-metadata
         * @return the version builder
         */
        public Builder withMeta(String meta) {
            this.meta = meta;
            return this;
        }

        /**
         * Build version.
         *
         * @return the {@link Version}
         * @throws VersionBuildException thrown if the version could not be built
         * @implSpec Major, Minor and Patch are required when building a valid semantic version.
         */
        public Version build() throws VersionBuildException {
            if (major == null)
                throw new VersionBuildException("Major version needs to be specified.");

            if (minor == null)
                throw new VersionBuildException("Minor version needs to be specified.");

            if (patch == null)
                throw new VersionBuildException("Patch version needs to be specified.");

            if (major < 0L)
                throw new VersionBuildException(String.format("Major version \"%s\" can't be less than 0.", major));

            if (minor < 0L)
                throw new VersionBuildException(String.format("Minor version \"%s\" can't be less than 0.", minor));

            if (patch < 0L)
                throw new VersionBuildException(String.format("Patch version \"%s\" can't be less than 0.", patch));

            if (preRelease == null)
                preRelease = "";

            if (meta == null)
                meta = "";

            return new Version(major, minor, patch, preRelease, meta);
        }
    }
}