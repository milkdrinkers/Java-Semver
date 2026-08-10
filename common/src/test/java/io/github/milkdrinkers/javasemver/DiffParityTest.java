package io.github.milkdrinkers.javasemver;

import io.github.milkdrinkers.javasemver.enums.ReleaseType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DiffParityTest {
    @Test
    void prereleaseOfMajorMilestoneToReleaseIsMajor() {
        Assertions.assertEquals(ReleaseType.MAJOR, Version.parse("1.0.0-a").difference(Version.parse("1.0.0")).get());
        Assertions.assertEquals(ReleaseType.MAJOR, Version.parse("1.0.0-a").difference(Version.parse("1.1.0")).get());
        Assertions.assertEquals(ReleaseType.MAJOR, Version.parse("1.0.0-a").difference(Version.parse("2.0.0")).get());
    }

    @Test
    void prereleaseToSameCoreReleaseUsesLowestChangedComponent() {
        Assertions.assertEquals(ReleaseType.MINOR, Version.parse("1.2.0-a").difference(Version.parse("1.2.0")).get());
        Assertions.assertEquals(ReleaseType.PATCH, Version.parse("1.2.3-a").difference(Version.parse("1.2.3")).get());
    }

    @Test
    void prereleaseToHigherReleaseUsesPlainReleaseType() {
        Assertions.assertEquals(ReleaseType.MINOR, Version.parse("1.2.0-a").difference(Version.parse("1.3.0")).get());
        Assertions.assertEquals(ReleaseType.PATCH, Version.parse("1.2.3-a").difference(Version.parse("1.2.4")).get());
    }

    @Test
    void prePrefixOnlyWhenHigherVersionIsPrerelease() {
        Assertions.assertEquals(ReleaseType.PREMAJOR, Version.parse("1.0.0").difference(Version.parse("2.0.0-a")).get());
        Assertions.assertEquals(ReleaseType.PREMINOR, Version.parse("1.0.0").difference(Version.parse("1.1.0-a")).get());
        Assertions.assertEquals(ReleaseType.PREPATCH, Version.parse("1.0.0").difference(Version.parse("1.0.1-a")).get());
    }

    @Test
    void differenceIsSymmetric() {
        Assertions.assertEquals(Version.parse("1.0.0-a").difference(Version.parse("1.0.0")), Version.parse("1.0.0").difference(Version.parse("1.0.0-a")));
    }

    @Test
    void plainAndPrereleaseOnlyCasesUnchanged() {
        Assertions.assertEquals(ReleaseType.MAJOR, Version.parse("1.0.0").difference(Version.parse("2.0.0")).get());
        Assertions.assertEquals(ReleaseType.PRERELEASE, Version.parse("1.0.0-a").difference(Version.parse("1.0.0-b")).get());
        Assertions.assertFalse(Version.parse("1.2.3").difference(Version.parse("1.2.3")).isPresent());
    }
}
