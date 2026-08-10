package io.github.milkdrinkers.javasemver;

import io.github.milkdrinkers.javasemver.exception.VersionParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class VersionParseTest {
    @Test
    void parsesFullVersion() {
        Version v = Version.parse("1.2.3-alpha.1+build.7");
        Assertions.assertEquals("1.2.3-alpha.1+build.7", v.toString());
    }

    @Test
    void stripsLeadingV() {
        Assertions.assertEquals(Version.of(1, 0, 0), Version.parse("v1.0.0"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.02.03", "1.a.3", "1.2", "1.2.3-", "1.2.3-alpha..1", "1.0.0-01"})
    void rejectsInvalid(String s) {
        Assertions.assertThrows(VersionParseException.class, () -> Version.parse(s));
        Assertions.assertFalse(Version.isValid(s));
        Assertions.assertFalse(Version.parseOptional(s).isPresent());
    }

    @Test
    void optionalAndValidAgreeOnGood() {
        Assertions.assertTrue(Version.isValid("1.2.3"));
        Assertions.assertTrue(Version.parseOptional("1.2.3").isPresent());
    }
}
