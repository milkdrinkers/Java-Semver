package io.github.milkdrinkers.javasemver;

import io.github.milkdrinkers.javasemver.exception.VersionBuildException;
import io.github.milkdrinkers.javasemver.exception.VersionParseException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A class representing a Semantic Version.
 */
public class Version extends VersionCompare implements Comparable<Version> {
    // Base fields
    private final long major; // The Major version
    private final long minor; // The Minor version
    private final long patch; // The Patch version
    private final String preRelease; // The pre-release data like "SNAPSHOT-1" or "RC-3"
    private final String[] preReleaseIdentifiers; // The pre-release data divided into strings by the seperator "."
    private final String meta; // The build-metadata

    // Cached fields
    private final boolean hasPreRelease;
    private final boolean hasMeta;
    private final String version; // The version consisting of only Major.Minor.Patch
    private final String versionFull; // The entire version string

    // Cached fields
    private final boolean isAlpha; // Whether the a pre-release contains "alpha"
    private final boolean isBeta; // Whether the a pre-release contains "beta"
    private final boolean isDevelopment; // Whether the a pre-release contains "dev", "develop" or "development"
    private final boolean isReleaseCandidate; // Whether the a pre-release contains "rc"
    private final boolean isSnapshot; // Whether the a pre-release contains "snapshot"

    /**
     * Instantiates a new version object.
     *
     * @param major the major version
     * @param minor the minor version
     * @param patch the patch version
     * @param preRelease the pre-release version
     * @param meta the build-meta
     * @implSpec Only to be used internally through builders/factories
     */
    @ApiStatus.Internal
    Version(long major, long minor, long patch, String preRelease, String meta) {
        // Base fields
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.preRelease = preRelease;
        this.preReleaseIdentifiers = preRelease.split("\\.");
        this.meta = meta;

        this.hasPreRelease = !getPreRelease().isEmpty();
        this.hasMeta = !getMeta().isEmpty();
        this.version = concatenateVersionString(this.major, this.minor, this.patch);
        this.versionFull = concatenateVersionStringFull(this.major, this.minor, this.patch, this.preRelease, this.meta);

        // Cached fields
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
     * @apiNote Uses {@link VersionParser#parse(String)} internally
     */
    public static @NotNull Version of(String version) throws VersionParseException {
        return VersionParser.parse(version);
    }

    /**
     * Create a Version object from semantic version data.
     *
     * @param major the major version
     * @param minor the minor version
     * @param patch the patch version
     * @param preRelease the pre-release version
     * @param meta the build-meta
     * @return the semantic version
     * @throws VersionBuildException thrown if parsing the data into a version failed
     * @apiNote Uses {@link VersionBuilder} internally
     */
    public static @NotNull Version of(long major, long minor, long patch, String preRelease, String meta) throws VersionBuildException {
        return new VersionBuilder()
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
     * Gets minor minor.
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
    public String getPreRelease() {
        return preRelease;
    }

    /**
     * Gets pre-release identifiers.
     *
     * @return the array of identifiers
     */
    public String[] getPreReleaseIdentifiers() {
        return preReleaseIdentifiers;
    }

    /**
     * Gets build-metadata.
     *
     * @return the metadata
     */
    public String getMeta() {
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
    public boolean hasMeta() {
        return hasMeta;
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
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param other the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(@NotNull Version other) {
        final VersionCheckResult result = VersionCompare.compare(this, other);

        switch (result) {
            case NEWER:
                return -1;
            case OLDER:
                return 1;
            default:
                return 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Version)) return false;
        Version version = (Version) o;
        return getMajor() == version.getMajor() && getMinor() == version.getMinor() && getPatch() == version.getPatch() && Objects.equals(hasPreRelease(), version.hasPreRelease()) && Objects.equals(getMeta(), version.getMeta());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMajor(), getMinor(), getPatch(), hasPreRelease(), getMeta());
    }

    @Override
    public String toString() {
        return "Version{" +
            "major=" + major +
            ", minor=" + minor +
            ", patch=" + patch +
            ", preRelease='" + preRelease + '\'' +
            ", meta='" + meta + '\'' +
            '}';
    }
}
