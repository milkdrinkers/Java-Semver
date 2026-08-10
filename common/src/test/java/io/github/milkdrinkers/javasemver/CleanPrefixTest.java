package io.github.milkdrinkers.javasemver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CleanPrefixTest {
    @Test
    void cleanStripsLeadingEqualsAndVRun() {
        Assertions.assertEquals("1.2.3", Version.clean("==v1.2.3").get());
        Assertions.assertEquals("1.2.3", Version.clean("v=1.2.3").get());
        Assertions.assertEquals("1.2.3", Version.clean("  =v1.2.3  ").get());
    }
}
