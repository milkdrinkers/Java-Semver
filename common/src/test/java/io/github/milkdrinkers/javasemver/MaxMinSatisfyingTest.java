package io.github.milkdrinkers.javasemver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class MaxMinSatisfyingTest {
    private final List<Version> versions = Arrays.asList(
        Version.parse("1.0.0"), Version.parse("1.2.0"),
        Version.parse("1.9.0"), Version.parse("2.0.0"));

    @Test
    void maxAndMin() {
        Assertions.assertEquals(Version.parse("1.9.0"), Range.maxSatisfying(versions, "^1.0.0").get());
        Assertions.assertEquals(Version.parse("1.0.0"), Range.minSatisfying(versions, "^1.0.0").get());
    }

    @Test
    void emptyWhenNoneMatch() {
        Assertions.assertFalse(Range.maxSatisfying(versions, ">=3.0.0").isPresent());
    }
}
