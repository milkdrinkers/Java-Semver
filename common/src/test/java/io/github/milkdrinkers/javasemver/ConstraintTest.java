package io.github.milkdrinkers.javasemver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConstraintTest {
    @Test
    void parsesAndTests() {
        Constraint c = Constraint.parse(">=1.2.3");
        Assertions.assertTrue(c.contains(Version.parse("1.2.3")));
        Assertions.assertTrue(c.contains(Version.parse("2.0.0")));
        Assertions.assertFalse(c.contains(Version.parse("1.0.0")));
        Assertions.assertEquals(">=1.2.3", c.toString());
    }

    @Test
    void bareVersionIsEquality() {
        Assertions.assertTrue(Constraint.parse("1.2.3").contains(Version.parse("1.2.3")));
        Assertions.assertFalse(Constraint.parse("1.2.3").contains(Version.parse("1.2.4")));
    }

    @Test
    void wildcardMatchesAnyRelease() {
        Assertions.assertTrue(Constraint.any().contains(Version.parse("9.9.9")));
    }
}
