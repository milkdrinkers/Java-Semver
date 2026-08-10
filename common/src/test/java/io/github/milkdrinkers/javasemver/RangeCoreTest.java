package io.github.milkdrinkers.javasemver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RangeCoreTest {
    @Test
    void andSet() {
        Range r = Range.parse(">=1.2.3 <2.0.0");
        Assertions.assertTrue(r.contains(Version.parse("1.5.0")));
        Assertions.assertFalse(r.contains(Version.parse("2.0.0")));
        Assertions.assertFalse(r.contains(Version.parse("1.0.0")));
    }

    @Test
    void orSets() {
        Range r = Range.parse("1.2.3 || >=2.0.0");
        Assertions.assertTrue(r.contains(Version.parse("1.2.3")));
        Assertions.assertTrue(r.contains(Version.parse("3.0.0")));
        Assertions.assertFalse(r.contains(Version.parse("1.5.0")));
    }

    @Test
    void starIsAny() {
        Assertions.assertTrue(Range.parse("*").isAny());
        Assertions.assertTrue(Range.parse("").isAny());
        Assertions.assertTrue(Range.parse("*").contains(Version.parse("1.0.0")));
    }
}
