package io.github.milkdrinkers.javasemver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AboveBelowTest {
    @Test
    void aboveAndBelow() {
        Assertions.assertTrue(Version.parse("3.0.0").isAbove(Range.parse("^1.0.0")));
        Assertions.assertTrue(Version.parse("0.5.0").isBelow(Range.parse("^1.0.0")));
        Assertions.assertFalse(Version.parse("1.5.0").isAbove(Range.parse("^1.0.0")));
        Assertions.assertFalse(Version.parse("1.5.0").isBelow(Range.parse("^1.0.0")));
    }
}
