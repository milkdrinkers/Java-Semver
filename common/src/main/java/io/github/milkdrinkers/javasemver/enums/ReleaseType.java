package io.github.milkdrinkers.javasemver.enums;

/**
 * Represents the type of version increment to perform.
 */
public enum ReleaseType {
    /**
     * Increment the major version number.
     * Example: 1.2.3 becomes 2.0.0
     */
    MAJOR,

    /**
     * Increment the minor version number.
     * Example: 1.2.3 becomes 1.3.0
     */
    MINOR,

    /**
     * Increment the patch version number.
     * Example: 1.2.3 becomes 1.2.4
     */
    PATCH,

    /**
     * Increment to the next major version with pre-release.
     * Example: 1.2.3 becomes 2.0.0-0
     */
    PREMAJOR,

    /**
     * Increment to the next minor version with pre-release.
     * Example: 1.2.3 becomes 1.3.0-0
     */
    PREMINOR,

    /**
     * Increment to the next patch version with pre-release.
     * Example: 1.2.3 becomes 1.2.4-0
     */
    PREPATCH,

    /**
     * Increment the pre-release version.
     * Example: 1.2.3-0 becomes 1.2.3-1, 1.2.3 becomes 1.2.4-0
     */
    PRERELEASE
}