package io.github.milkdrinkers.javasemver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class VersionLooseParseTest {
    @Test
    void tolerantOfWhitespacePrefixAndLeadingZeros() {
        Assertions.assertEquals(Version.of(1, 2, 3), Version.parseLoose("  =v1.2.3 "));
        Assertions.assertEquals(Version.of(1, 2, 3), Version.parseLoose("01.02.03"));
        Assertions.assertEquals("1.2.3-beta", Version.parseLoose("v1.2.3-beta").toString());
    }

    @Test
    void strictStillRejectsLeadingZeros() {
        Assertions.assertFalse(Version.isValid("01.02.03"));
    }
}
