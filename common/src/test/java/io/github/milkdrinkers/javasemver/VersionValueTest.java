package io.github.milkdrinkers.javasemver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class VersionValueTest {
    @Test
    void exposesComponents() {
        Version v = Version.of(1, 2, 3, "alpha.1", "build.7");
        Assertions.assertEquals(1, v.getMajor());
        Assertions.assertEquals(2, v.getMinor());
        Assertions.assertEquals(3, v.getPatch());
        Assertions.assertEquals("alpha.1", v.getPreRelease());
        Assertions.assertEquals(Arrays.asList("alpha", "1"), v.getPreReleaseIdentifiers());
        Assertions.assertEquals("build.7", v.getBuildMetadata());
        Assertions.assertTrue(v.hasPreRelease());
        Assertions.assertTrue(v.hasBuildMetadata());
        Assertions.assertEquals("1.2.3-alpha.1+build.7", v.toString());
    }

    @Test
    void identifiersListIsUnmodifiable() {
        Version v = Version.of(1, 0, 0, "a.b");
        Assertions.assertThrows(UnsupportedOperationException.class, () -> v.getPreReleaseIdentifiers().add("c"));
    }

    @Test
    void structuralEqualityIncludesBuildMetadata() {
        Version a = Version.of(1, 0, 0, "", "build.1");
        Version b = Version.of(1, 0, 0, "", "build.2");
        Assertions.assertNotEquals(a, b);
        Assertions.assertEquals(Version.of(1, 0, 0), Version.of(1, 0, 0));
    }
}
