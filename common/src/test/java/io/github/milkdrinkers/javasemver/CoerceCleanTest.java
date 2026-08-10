package io.github.milkdrinkers.javasemver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CoerceCleanTest {
    @Test
    void coerceExtractsVersions() {
        Assertions.assertEquals(Version.of(1, 2, 0), Version.coerce("v1.2").get());
        Assertions.assertEquals(Version.of(1, 2, 3), Version.coerce("app-1.2.3.jar").get());
        Assertions.assertEquals(Version.of(4, 0, 0), Version.coerce("release 4").get());
        Assertions.assertFalse(Version.coerce("no numbers here").isPresent());
    }

    @Test
    void cleanNormalizesValidStrings() {
        Assertions.assertEquals("1.2.3", Version.clean("  =v1.2.3  ").get());
        Assertions.assertFalse(Version.clean("not a version").isPresent());
    }

    /**
     * Coercion fails on overflowing numbers returns empty optional.
     */
    @Test
    void coerceOverflowingNumber() {
        Assertions.assertFalse(Version.coerce("build-99999999999999999999.jar").isPresent());
    }
}
