package io.github.milkdrinkers.javasemver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SatisfiesTest {
    @Test
    void satisfiesFromVersionSide() {
        Assertions.assertTrue(Version.parse("1.2.3").satisfies("^1.0.0"));
        Assertions.assertTrue(Version.parse("1.2.3").satisfies(Range.parse("~1.2.0")));
        Assertions.assertFalse(Version.parse("2.0.0").satisfies("^1.0.0"));
    }

    @Test
    void rangeParseLooseAcceptsVPrefixedBounds() {
        Assertions.assertTrue(Range.parseLoose(">=v1.0.0").contains(Version.parse("1.2.0")));
    }
}
