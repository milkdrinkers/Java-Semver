package io.github.milkdrinkers.javasemver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class IntersectsTest {
    @Test
    void overlapping() {
        Assertions.assertTrue(Range.parse(">=1.0.0 <2.0.0").intersects(Range.parse("^1.5.0")));
        Assertions.assertTrue(Range.parse("*").intersects(Range.parse("^1.0.0")));
    }

    @Test
    void disjoint() {
        Assertions.assertFalse(Range.parse("^1.0.0").intersects(Range.parse("^2.0.0")));
        Assertions.assertFalse(Range.parse("<1.0.0").intersects(Range.parse(">=2.0.0")));
    }
}
