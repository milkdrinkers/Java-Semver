package io.github.milkdrinkers.javasemver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BuildOrderingTest {
    /**
     * Compares build identifiers with leading zeros vs non leading zeros
     */
    @Test
    void leadingZerosCompareNumerically() {
        Assertions.assertEquals(0, Version.BUILD_AWARE.compare(Version.parse("1.0.0+007"), Version.parse("1.0.0+7")));
        Assertions.assertTrue(Version.BUILD_AWARE.compare(Version.parse("1.0.0+007"), Version.parse("1.0.0+10")) < 0);
        Assertions.assertTrue(Version.BUILD_AWARE.compare(Version.parse("1.0.0+010"), Version.parse("1.0.0+9")) > 0);
    }
}
