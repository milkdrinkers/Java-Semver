package io.github.milkdrinkers.javasemver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ParseTrimTest {
    @Test
    void strictParseTrimsSurroundingWhitespace() {
        Assertions.assertEquals(Version.of(1, 2, 3), Version.parse("  1.2.3 "));
        Assertions.assertEquals(Version.of(1, 2, 3), Version.parse("\t1.2.3\n"));
        Assertions.assertEquals(Version.of(1, 2, 3), Version.parse(" v1.2.3 "));
    }

    @Test
    void isValidAcceptsPaddedVersion() {
        Assertions.assertTrue(Version.isValid(" 1.2.3 "));
    }
}
