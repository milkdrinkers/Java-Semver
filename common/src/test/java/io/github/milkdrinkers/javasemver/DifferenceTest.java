package io.github.milkdrinkers.javasemver;

import io.github.milkdrinkers.javasemver.enums.ReleaseType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DifferenceTest {
    @Test
    void classifiesDifference() {
        Assertions.assertEquals(ReleaseType.MAJOR, Version.parse("1.0.0").difference(Version.parse("2.0.0")).get());
        Assertions.assertEquals(ReleaseType.MINOR, Version.parse("1.0.0").difference(Version.parse("1.1.0")).get());
        Assertions.assertEquals(ReleaseType.PATCH, Version.parse("1.0.0").difference(Version.parse("1.0.1")).get());
        Assertions.assertEquals(ReleaseType.PREMAJOR, Version.parse("1.0.0").difference(Version.parse("2.0.0-a")).get());
        Assertions.assertEquals(ReleaseType.PRERELEASE, Version.parse("1.0.0-a").difference(Version.parse("1.0.0-b")).get());
        Assertions.assertFalse(Version.parse("1.0.0").difference(Version.parse("1.0.0")).isPresent());
    }
}
