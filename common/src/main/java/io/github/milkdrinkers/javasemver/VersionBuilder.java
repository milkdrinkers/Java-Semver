package io.github.milkdrinkers.javasemver;

import io.github.milkdrinkers.javasemver.exception.VersionBuildException;

/**
 * A class allowing the creation of {@link Version} objects through this builder.
 *
 * @implSpec Major, Minor and Patch are required in a valid semantic version.
 */
public final class VersionBuilder {
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
    public VersionBuilder withMajor(long major) {
        this.major = major;
        return this;
    }

    /**
     * Set minor version for builder.
     *
     * @param minor the minor
     * @return the version builder
     */
    public VersionBuilder withMinor(long minor) {
        this.minor = minor;
        return this;
    }

    /**
     * Set patch version for builder.
     *
     * @param patch the patch
     * @return the version builder
     */
    public VersionBuilder withPatch(long patch) {
        this.patch = patch;
        return this;
    }

    /**
     * Set pre-release version for builder.
     *
     * @param preRelease the pre-release
     * @return the version builder
     */
    public VersionBuilder withPreRelease(String preRelease) {
        this.preRelease = preRelease;
        return this;
    }

    /**
     * Set build-metadata version for builder.
     *
     * @param meta the build-metadata
     * @return the version builder
     */
    public VersionBuilder withMeta(String meta) {
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