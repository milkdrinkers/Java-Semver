package io.github.milkdrinkers.javasemver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GtrLtrParityTest {
    private static boolean gtr(String v, String r) {
        return Version.parse(v).isAbove(Range.parse(r));
    }

    private static boolean ltr(String v, String r) {
        return Version.parse(v).isBelow(Range.parse(r));
    }

    @Test
    void prereleaseInsideRangeIsReportedBothAboveAndBelow() {
        Assertions.assertTrue(gtr("1.5.0-alpha", "^1.0.0"));
        Assertions.assertTrue(ltr("1.5.0-alpha", "^1.0.0"));
    }

    @Test
    void ordinaryCasesMatchNode() {
        Assertions.assertTrue(gtr("3.0.0", "^1.0.0"));
        Assertions.assertFalse(ltr("3.0.0", "^1.0.0"));
        Assertions.assertFalse(gtr("0.5.0", "^1.0.0"));
        Assertions.assertTrue(ltr("0.5.0", "^1.0.0"));
        Assertions.assertTrue(gtr("2.0.0", "^1.0.0"));
        Assertions.assertTrue(gtr("2.0.0-0", "^1.0.0"));
        Assertions.assertTrue(gtr("1.3.0", "~1.2.3"));
        Assertions.assertFalse(gtr("1.2.2", "~1.2.3"));
        Assertions.assertTrue(ltr("1.2.2", "~1.2.3"));
    }

    @Test
    void satisfyingVersionIsNeitherAboveNorBelow() {
        Assertions.assertFalse(gtr("2.0.0", ">=1.0.0"));
        Assertions.assertFalse(ltr("2.0.0", ">=1.0.0"));
        Assertions.assertFalse(gtr("0.5.0", "<2.0.0"));
    }

    @Test
    void versionInGapBetweenOrSetsIsNeitherAboveNorBelow() {
        Assertions.assertFalse(gtr("2.0.0", "1.x || 3.x"));
        Assertions.assertFalse(ltr("2.0.0", "1.x || 3.x"));
        Assertions.assertFalse(gtr("2.5.0", "1.x || 3.x"));
    }
}
