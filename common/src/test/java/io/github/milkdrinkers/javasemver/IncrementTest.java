package io.github.milkdrinkers.javasemver;

import io.github.milkdrinkers.javasemver.enums.ReleaseType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class IncrementTest {
    @Test
    void plainIncrements() {
        Assertions.assertEquals("2.0.0", Version.parse("1.2.3").increment(ReleaseType.MAJOR).toString());
        Assertions.assertEquals("1.3.0", Version.parse("1.2.3").increment(ReleaseType.MINOR).toString());
        Assertions.assertEquals("1.2.4", Version.parse("1.2.3").increment(ReleaseType.PATCH).toString());
    }

    @Test
    void prereleaseIncrements() {
        Assertions.assertEquals("1.2.4-0", Version.parse("1.2.3").increment(ReleaseType.PRERELEASE).toString());
        Assertions.assertEquals("1.2.4-alpha.0", Version.parse("1.2.3").increment(ReleaseType.PREPATCH, "alpha").toString());
        Assertions.assertEquals("1.2.3-alpha.2", Version.parse("1.2.3-alpha.1").increment(ReleaseType.PRERELEASE, "alpha").toString());
    }

    @Test
    void majorOfPrereleaseAtZeroDropsPrerelease() {
        Assertions.assertEquals("2.0.0", Version.parse("2.0.0-alpha.1").increment(ReleaseType.MAJOR).toString());
    }
}
