package io.github.milkdrinkers.javasemver.internal;

import io.github.milkdrinkers.javasemver.Constraint;
import io.github.milkdrinkers.javasemver.Version;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class IntervalTest {
    @Test
    void combinesBounds() {
        Interval i = Interval.of(Arrays.asList(Constraint.parse(">=1.2.0"), Constraint.parse("<2.0.0")));
        Assertions.assertEquals(Version.parse("1.2.0"), i.getLower());
        Assertions.assertTrue(i.isLowerInclusive());
        Assertions.assertEquals(Version.parse("2.0.0"), i.getUpper());
        Assertions.assertFalse(i.isUpperInclusive());
        Assertions.assertFalse(i.isEmpty());
    }

    @Test
    void contradictionIsEmpty() {
        Interval i = Interval.of(Arrays.asList(Constraint.parse(">=2.0.0"), Constraint.parse("<1.0.0")));
        Assertions.assertTrue(i.isEmpty());
    }

    @Test
    void overlapDetection() {
        Interval a = Interval.of(Arrays.asList(Constraint.parse(">=1.0.0"), Constraint.parse("<2.0.0")));
        Interval b = Interval.of(Arrays.asList(Constraint.parse(">=1.5.0"), Constraint.parse("<3.0.0")));
        Interval c = Interval.of(Arrays.asList(Constraint.parse(">=2.0.0"), Constraint.parse("<3.0.0")));
        Assertions.assertTrue(Interval.overlaps(a, b));
        Assertions.assertFalse(Interval.overlaps(a, c));
    }
}
