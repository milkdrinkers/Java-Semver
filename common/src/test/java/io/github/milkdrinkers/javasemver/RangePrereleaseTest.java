package io.github.milkdrinkers.javasemver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RangePrereleaseTest {
    @Test
    void prereleaseMatchesOnlyWhenNamed() {
        Assertions.assertTrue(Range.parse(">=1.2.3-alpha <2.0.0").contains(Version.parse("1.2.3-alpha.4")));
        Assertions.assertFalse(Range.parse(">=1.0.0 <2.0.0").contains(Version.parse("1.2.3-alpha.4")));
    }

    @Test
    void stableUnaffected() {
        Assertions.assertTrue(Range.parse(">=1.0.0 <2.0.0").contains(Version.parse("1.5.0")));
    }
}
