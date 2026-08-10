package io.github.milkdrinkers.javasemver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class VersionComparisonTest {
    @Test
    void precedenceOrdering() {
        Assertions.assertTrue(Version.parse("1.0.0-alpha").isLessThan(Version.parse("1.0.0-alpha.1")));
        Assertions.assertTrue(Version.parse("1.0.0-rc.1").isLessThan(Version.parse("1.0.0")));
        Assertions.assertTrue(Version.parse("2.0.0").isGreaterThan(Version.parse("1.9.9")));
    }

    @Test
    void predicates() {
        Version a = Version.parse("1.2.3");
        Assertions.assertTrue(a.isAtLeast(Version.parse("1.2.3")));
        Assertions.assertTrue(a.isAtMost(Version.parse("1.2.3")));
        Assertions.assertTrue(a.isEqualTo(Version.parse("1.2.3")));
        Assertions.assertFalse(a.isGreaterThan(Version.parse("1.2.3")));
    }

    @Test
    void buildMetadataIgnoredForPrecedenceButUsedByBuildAware() {
        Version a = Version.parse("1.0.0+build.1");
        Version b = Version.parse("1.0.0+build.2");
        Assertions.assertEquals(0, a.compareTo(b));
        Assertions.assertTrue(a.isEqualTo(b));
        Assertions.assertTrue(Version.BUILD_AWARE.compare(a, b) < 0);
    }
}
