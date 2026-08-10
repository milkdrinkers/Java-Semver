package io.github.milkdrinkers.javasemver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CoerceParityTest {
    private static String coerce(String s) {
        return Version.coerce(s).map(Version::toString).orElse(null);
    }

    @Test
    void matchesNodeCoerce() {
        Assertions.assertEquals("1.2.0", coerce("v1.2"));
        Assertions.assertEquals("1.2.3", coerce("1.2.3.4.5"));
        Assertions.assertEquals("2.3.0", coerce(".2.3"));
        Assertions.assertEquals("10.0.0", coerce("10.0.0.0"));
        Assertions.assertEquals("1.0.0", coerce("a1b2c3"));
        Assertions.assertEquals("2.3.0", coerce("version 2.3"));
        Assertions.assertEquals("1.2.0", coerce("1.2.x"));
    }

    @Test
    void leadingZerosMakeCoerceFail() {
        Assertions.assertNull(coerce("01.02.03"));
    }

    @Test
    void overlongNumbersAreSkipped() {
        Assertions.assertEquals("5.6.0", coerce("123456789012345678.5.6"));
        Assertions.assertEquals("0.0.0", coerce("99999999999999999999.0.0"));
    }

    @Test
    void noDigitsYieldsEmpty() {
        Assertions.assertNull(coerce("x.y.z"));
        Assertions.assertNull(coerce("no numbers here"));
    }
}
