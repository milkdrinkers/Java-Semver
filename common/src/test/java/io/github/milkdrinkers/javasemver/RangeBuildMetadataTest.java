package io.github.milkdrinkers.javasemver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Build metadata never affects matching in ranges so range comparators with it must parse and behave as if it were absent.
 */
class RangeBuildMetadataTest {
    @Test
    void tildeToleratesBuildMetadata() {
        Assertions.assertTrue(Version.parse("1.2.9").satisfies("~1.2.3+build"));
        Assertions.assertFalse(Version.parse("1.3.0").satisfies("~1.2.3+build"));
    }

    @Test
    void caretToleratesBuildMetadata() {
        Assertions.assertTrue(Version.parse("1.9.0").satisfies("^1.2.3+build"));
        Assertions.assertFalse(Version.parse("2.0.0").satisfies("^1.2.3+build"));
    }

    @Test
    void hyphenToleratesBuildMetadataOnEitherBound() {
        Assertions.assertTrue(Version.parse("2.3.4").satisfies("1.2.3+build - 2.3.4"));
        Assertions.assertTrue(Version.parse("2.3.4").satisfies("1.2.3 - 2.3.4+build"));
        Assertions.assertFalse(Version.parse("2.3.5").satisfies("1.2.3 - 2.3.4+build"));
    }

    @Test
    void caretWithPrereleaseAndBuildMetadata() {
        Assertions.assertTrue(Version.parse("1.2.3-beta.2").satisfies("^1.2.3-beta+build"));
    }
}
