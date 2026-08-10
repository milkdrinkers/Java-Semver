package io.github.milkdrinkers.javasemver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MinVersionTest {
    @Test
    void lowestSatisfying() {
        Assertions.assertEquals(Version.parse("1.2.0"), Range.parse("^1.2.0").minVersion().get());
        Assertions.assertEquals(Version.parse("1.2.4"), Range.parse(">1.2.3").minVersion().get());
        Assertions.assertEquals(Version.parse("0.0.0"), Range.parse("*").minVersion().get());
    }

    @Test
    void emptyRangeHasNoMin() {
        Assertions.assertFalse(Range.parse(">=2.0.0 <1.0.0").minVersion().isPresent());
    }

    @Test
    void exclusiveLowerBoundWithPrereleaseAppendsZeroIdentifier() {
        Assertions.assertEquals(Version.parse("1.2.3-alpha.0"), Range.parse(">1.2.3-alpha").minVersion().get());
    }

    @Test
    void exclusiveLowerBoundWithoutPrereleaseStillBumpsPatch() {
        Assertions.assertEquals(Version.parse("1.2.4"), Range.parse(">1.2.3").minVersion().get());
    }
}
