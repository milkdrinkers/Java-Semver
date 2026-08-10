package io.github.milkdrinkers.javasemver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RangeHyphenTest {
    @Test
    void fullHyphen() {
        Range r = Range.parse("1.2.3 - 2.3.4");
        Assertions.assertTrue(r.contains(Version.parse("1.2.3")));
        Assertions.assertTrue(r.contains(Version.parse("2.3.4")));
        Assertions.assertFalse(r.contains(Version.parse("2.3.5")));
    }

    @Test
    void partialUpperBoundExpands() {
        Range r = Range.parse("1.2.3 - 2.3");
        Assertions.assertTrue(r.contains(Version.parse("2.3.9")));
        Assertions.assertFalse(r.contains(Version.parse("2.4.0")));
    }

    @Test
    void partialLowerBound() {
        Range r = Range.parse("1.2 - 2.3.4");
        Assertions.assertTrue(r.contains(Version.parse("1.2.0")));
        Assertions.assertFalse(r.contains(Version.parse("1.1.9")));
    }
}
