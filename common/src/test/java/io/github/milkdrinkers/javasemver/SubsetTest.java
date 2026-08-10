package io.github.milkdrinkers.javasemver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SubsetTest {
    @Test
    void containment() {
        Assertions.assertTrue(Range.parse("^1.2.0").isSubsetOf(Range.parse(">=1.0.0 <2.0.0")));
        Assertions.assertTrue(Range.parse(">=1.5.0 <1.8.0").isSubsetOf(Range.parse("^1.0.0")));
        Assertions.assertTrue(Range.parse("^1.0.0").isSubsetOf(Range.parse("*")));
    }

    @Test
    void notContained() {
        Assertions.assertFalse(Range.parse("^1.0.0").isSubsetOf(Range.parse(">=1.5.0 <2.0.0")));
        Assertions.assertFalse(Range.parse("*").isSubsetOf(Range.parse("^1.0.0")));
    }
}
