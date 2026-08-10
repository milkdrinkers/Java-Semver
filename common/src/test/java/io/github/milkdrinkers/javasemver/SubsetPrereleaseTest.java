package io.github.milkdrinkers.javasemver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SubsetPrereleaseTest {
    @Test
    void prereleaseBoundRequiresMatchingTupleInDominator() {
        Assertions.assertFalse(Range.parse(">=1.2.3-pre").isSubsetOf(Range.parse(">=1.0.0")));
        Assertions.assertFalse(Range.parse("^1.2.3-beta").isSubsetOf(Range.parse(">=1.0.0 <2.0.0")));
    }

    @Test
    void prereleaseBoundSubsetWhenDominatorNamesSameTuple() {
        Assertions.assertTrue(Range.parse(">=1.2.3-pre <2.0.0").isSubsetOf(Range.parse(">=1.2.3-pre <3.0.0")));
    }

    @Test
    void caretUpperZeroPrereleaseBoundDoesNotTriggerRequirement() {
        Assertions.assertTrue(Range.parse("^1.2.3").isSubsetOf(Range.parse(">=1.0.0 <2.0.0")));
        Assertions.assertTrue(Range.parse("~1.2.3").isSubsetOf(Range.parse(">=1.2.0 <1.3.0")));
    }
}
