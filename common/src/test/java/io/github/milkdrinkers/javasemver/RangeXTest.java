package io.github.milkdrinkers.javasemver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RangeXTest {
    @Test
    void minorWildcard() {
        Range r = Range.parse("1.x");
        Assertions.assertTrue(r.contains(Version.parse("1.9.9")));
        Assertions.assertFalse(r.contains(Version.parse("2.0.0")));
    }

    @Test
    void patchWildcard() {
        Range r = Range.parse("1.2.x");
        Assertions.assertTrue(r.contains(Version.parse("1.2.9")));
        Assertions.assertFalse(r.contains(Version.parse("1.3.0")));
    }

    @Test
    void partialMeansWildcard() {
        Assertions.assertTrue(Range.parse("1").contains(Version.parse("1.4.2")));
        Assertions.assertTrue(Range.parse("1.2").contains(Version.parse("1.2.7")));
    }
}
