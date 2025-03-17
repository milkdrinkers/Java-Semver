import io.github.milkdrinkers.javasemver.Version;
import io.github.milkdrinkers.javasemver.exception.VersionParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

public class SemVerTest {

    @Nested
    class ParsingTests {
        @Test
        public void testBasicParsing() {
            Version v = Version.of("1.2.3");
            Assertions.assertEquals(1, v.getMajor());
            Assertions.assertEquals(2, v.getMinor());
            Assertions.assertEquals(3, v.getPatch());
            Assertions.assertEquals("", v.getPreRelease());
            Assertions.assertEquals("", v.getBuildMetadata());
        }

        @Test
        public void testParsingWithPreRelease() {
            Version v = Version.of("1.2.3-alpha.1");
            Assertions.assertEquals(1, v.getMajor());
            Assertions.assertEquals(2, v.getMinor());
            Assertions.assertEquals(3, v.getPatch());
            Assertions.assertEquals("alpha.1", v.getPreRelease());
            Assertions.assertEquals("", v.getBuildMetadata());
        }

        @Test
        public void testParsingWithBuildMetadata() {
            Version v = Version.of("1.2.3+build.20230101");
            Assertions.assertEquals(1, v.getMajor());
            Assertions.assertEquals(2, v.getMinor());
            Assertions.assertEquals(3, v.getPatch());
            Assertions.assertEquals("", v.getPreRelease());
            Assertions.assertEquals("build.20230101", v.getBuildMetadata());
        }

        @Test
        public void testParsingWithPreReleaseAndBuildMetadata() {
            Version v = Version.of("1.2.3-alpha.1+build.20230101");
            Assertions.assertEquals(1, v.getMajor());
            Assertions.assertEquals(2, v.getMinor());
            Assertions.assertEquals(3, v.getPatch());
            Assertions.assertEquals("alpha.1", v.getPreRelease());
            Assertions.assertEquals("build.20230101", v.getBuildMetadata());
        }

        @Test
        public void testParsingWithLeadingZeros() {
            Assertions.assertThrows(VersionParseException.class, () -> Version.of("1.02.03"));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "1.4.0.0",
            "1.a.3",
            "01.2.3",
            "1.2.3-",
            "1.2.3+",
            "1.2.3-alpha..1",
            "1.2.3-alpha_1",
            ".1.2.3",
            "1.2",
            "-1.2.3"
        })
        public void testInvalidVersions(String version) {
            Assertions.assertThrows(VersionParseException.class, () -> Version.of(version));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "0.0.0",
            "999999.999999.999999",
            "1.2.3-alpha.1.0.1",
            "1.2.3-alpha-1.beta",
            "1.2.3+build.1-2.3-4",
            "1.2.3-rc.1+build.123"
        })
        public void testValidVersions(String version) {
            Assertions.assertDoesNotThrow(() -> Version.of(version));
        }
    }

    @Nested
    class ComparisonTests {
        @Test
        public void testReleaseVsPreRelease() {
            Version release = Version.of("1.0.0");
            Version preRelease = Version.of("1.0.0-alpha");

            Assertions.assertTrue(Version.isNewer(release, preRelease));
            Assertions.assertTrue(Version.isNewerOrEqual(release, preRelease));
            Assertions.assertFalse(Version.isEqual(release, preRelease));
            Assertions.assertFalse(Version.isOlderOrEqual(release, preRelease));
            Assertions.assertFalse(Version.isOlder(release, preRelease));
        }

        @Test
        public void testPreReleaseComparisons() {
            Version v1 = Version.of("1.0.0-alpha");
            Version v2 = Version.of("1.0.0-alpha.1");
            Version v3 = Version.of("1.0.0-alpha.beta");
            Version v4 = Version.of("1.0.0-beta");
            Version v5 = Version.of("1.0.0-beta.2");
            Version v6 = Version.of("1.0.0-beta.11");
            Version v7 = Version.of("1.0.0-rc.1");

            Assertions.assertTrue(Version.isOlder(v1, v2));
            Assertions.assertTrue(Version.isOlder(v2, v3));
            Assertions.assertTrue(Version.isOlder(v3, v4));
            Assertions.assertTrue(Version.isOlder(v4, v5));
            Assertions.assertTrue(Version.isOlder(v5, v6));
            Assertions.assertTrue(Version.isOlder(v6, v7));
        }

        @Test
        public void testBuildMetadataIgnored() {
            Version v1 = Version.of("1.0.0+build.1");
            Version v2 = Version.of("1.0.0+build.2");

            Assertions.assertTrue(Version.isEqual(v1, v2));
            Assertions.assertTrue(Version.isNewerOrEqual(v1, v2));
            Assertions.assertTrue(Version.isOlderOrEqual(v1, v2));
            Assertions.assertFalse(Version.isNewer(v1, v2));
            Assertions.assertFalse(Version.isOlder(v1, v2));
        }

        @Test
        public void testPreReleaseWithBuildMetadata() {
            Version v1 = Version.of("1.0.0-alpha+build.1");
            Version v2 = Version.of("1.0.0-alpha+build.2");

            Assertions.assertTrue(Version.isEqual(v1, v2));

            Version v3 = Version.of("1.0.0-alpha.1+build.1");
            Assertions.assertTrue(Version.isNewer(v3, v1));
        }

        @Test
        public void testNumericVsAlphabeticPreRelease() {
            Version v1 = Version.of("1.0.0-alpha.1");
            Version v2 = Version.of("1.0.0-alpha.beta");

            Assertions.assertTrue(Version.isOlder(v1, v2));

            Version v3 = Version.of("1.0.0-beta.2");
            Version v4 = Version.of("1.0.0-beta.11");

            Assertions.assertTrue(Version.isOlder(v3, v4));
        }

        @ParameterizedTest
        @MethodSource("provideVersionPairs")
        public void testVersionOrder(String olderVersion, String newerVersion) {
            Version older = Version.of(olderVersion);
            Version newer = Version.of(newerVersion);

            Assertions.assertTrue(Version.isOlder(older, newer),
                olderVersion + " should be older than " + newerVersion);
            Assertions.assertTrue(Version.isNewer(newer, older),
                newerVersion + " should be newer than " + olderVersion);
            Assertions.assertFalse(Version.isEqual(older, newer));
        }

        static Stream<Arguments> provideVersionPairs() {
            return Stream.of(
                Arguments.of("0.0.1", "0.0.2"),
                Arguments.of("0.1.0", "0.2.0"),
                Arguments.of("1.0.0", "2.0.0"),
                Arguments.of("1.2.3", "1.2.4"),
                Arguments.of("1.2.3", "1.3.0"),
                Arguments.of("1.2.3", "2.0.0"),
                Arguments.of("1.0.0-alpha", "1.0.0"),
                Arguments.of("1.0.0-alpha", "1.0.0-alpha.1"),
                Arguments.of("1.0.0-alpha.1", "1.0.0-alpha.beta"),
                Arguments.of("1.0.0-alpha.beta", "1.0.0-beta"),
                Arguments.of("1.0.0-beta", "1.0.0-beta.2"),
                Arguments.of("1.0.0-beta.2", "1.0.0-beta.11"),
                Arguments.of("1.0.0-beta.11", "1.0.0-rc.1"),
                Arguments.of("1.0.0-rc.1", "1.0.0")
            );
        }
    }

    @Nested
    class EquivalenceTests {
        @Test
        public void testIdenticalVersionsEqual() {
            Version v1 = Version.of("1.2.3");
            Version v2 = Version.of("1.2.3");

            Assertions.assertTrue(Version.isEqual(v1, v2));
            Assertions.assertTrue(Version.isNewerOrEqual(v1, v2));
            Assertions.assertTrue(Version.isOlderOrEqual(v1, v2));
            Assertions.assertFalse(Version.isNewer(v1, v2));
            Assertions.assertFalse(Version.isOlder(v1, v2));
        }

        @Test
        public void testVersionsWithDifferentBuildMetadataEqual() {
            Version v1 = Version.of("1.2.3+build.1");
            Version v2 = Version.of("1.2.3+build.2");

            Assertions.assertTrue(Version.isEqual(v1, v2));
        }

        @Test
        public void testVersionsWithSamePreReleaseEqual() {
            Version v1 = Version.of("1.2.3-alpha");
            Version v2 = Version.of("1.2.3-alpha");

            Assertions.assertTrue(Version.isEqual(v1, v2));
        }

        @Test
        public void testVersionsWithSamePreReleaseAndDifferentBuildMetadataEqual() {
            Version v1 = Version.of("1.2.3-alpha+build.1");
            Version v2 = Version.of("1.2.3-alpha+build.2");

            Assertions.assertTrue(Version.isEqual(v1, v2));
        }
    }

    @Nested
    class EdgeCaseTests {
        @Test
        public void testZeroVersions() {
            Version v1 = Version.of("0.0.0");
            Version v2 = Version.of("0.0.1");

            Assertions.assertTrue(Version.isOlder(v1, v2));
        }

        @Test
        public void testVeryLargeNumbers() {
            Version v1 = Version.of("999999.999999.999999");
            Version v2 = Version.of("1000000.0.0");

            Assertions.assertTrue(Version.isOlderOrEqual(v1, v2));
        }

        @Test
        public void testNumericPreReleaseIdentifiers() {
            Version v1 = Version.of("1.0.0-1");
            Version v2 = Version.of("1.0.0-2");

            Assertions.assertTrue(Version.isOlder(v1, v2));
        }

        @Test
        public void testAlphanumericPreReleaseIdentifiers() {
            Version v1 = Version.of("1.0.0-a1");
            Version v2 = Version.of("1.0.0-a2");

            Assertions.assertTrue(Version.isOlder(v1, v2));
        }

        @Test
        public void testDifferentLengthPreReleaseIdentifiers() {
            Version v1 = Version.of("1.0.0-alpha");
            Version v2 = Version.of("1.0.0-alpha.1");

            Assertions.assertTrue(Version.isOlder(v1, v2));
        }

        @Test
        public void testSpecialPreReleaseIdentifiers() {
            Version snapshot = Version.of("2.0.0-snapshot");
            Version beta = Version.of("2.0.0-beta.2");
            Version release = Version.of("2.0.0");

            Assertions.assertTrue(Version.isNewer(snapshot, beta));
            Assertions.assertTrue(Version.isNewer(release, snapshot));
        }
    }

    @Nested
    class PreReleaseIdentifierOrderingTests {
        @Test
        public void testNumericIdentifiersComparedNumerically() {
            Version v1 = Version.of("1.0.0-1");
            Version v2 = Version.of("1.0.0-alpha");

            Assertions.assertTrue(Version.isOlder(v1, v2));

            Version v3 = Version.of("1.0.0-2");
            Version v4 = Version.of("1.0.0-10");

            Assertions.assertTrue(Version.isOlder(v3, v4));
        }

        @Test
        public void testIdentifiersWithLettersComparedLexically() {
            Version v1 = Version.of("1.0.0-alpha");
            Version v2 = Version.of("1.0.0-beta");

            Assertions.assertTrue(Version.isOlder(v1, v2));

            Version v3 = Version.of("1.0.0-abc");
            Version v4 = Version.of("1.0.0-abd");

            Assertions.assertTrue(Version.isOlder(v3, v4));
        }

        @Test
        public void testIdentifiersWithSamePrefixComparedByLength() {
            Version v1 = Version.of("1.0.0-alpha");
            Version v2 = Version.of("1.0.0-alpha.1");

            Assertions.assertTrue(Version.isOlder(v1, v2));

            Version v3 = Version.of("1.0.0-alpha.beta");
            Version v4 = Version.of("1.0.0-alpha.beta.1");

            Assertions.assertTrue(Version.isOlder(v3, v4));
        }
    }

    @Nested
    class ComplexComparisonTests {
        @Test
        public void testMixedVersionComparisons() {
            Version[] versions = new Version[]{
                Version.of("1.0.0"),
                Version.of("1.0.1"),
                Version.of("1.1.0"),
                Version.of("1.1.1"),
                Version.of("2.0.0"),
                Version.of("2.1.0")
            };

            for (int i = 0; i < versions.length - 1; i++) {
                for (int j = i + 1; j < versions.length; j++) {
                    Assertions.assertTrue(Version.isOlder(versions[i], versions[j]));
                    Assertions.assertTrue(Version.isNewer(versions[j], versions[i]));
                }
            }
        }

        @Test
        public void testPreReleaseVsReleaseForSameVersion() {
            Version[] versions = new Version[]{
                Version.of("1.0.0-alpha"),
                Version.of("1.0.0-alpha.1"),
                Version.of("1.0.0-beta"),
                Version.of("1.0.0-beta.2"),
                Version.of("1.0.0-beta.11"),
                Version.of("1.0.0-rc.1"),
                Version.of("1.0.0")
            };

            for (int i = 0; i < versions.length - 1; i++) {
                for (int j = i + 1; j < versions.length; j++) {
                    Assertions.assertTrue(Version.isOlder(versions[i], versions[j]));
                    Assertions.assertTrue(Version.isNewer(versions[j], versions[i]));
                }
            }
        }

        @Test
        public void testPreReleaseVsHigherVersion() {
            Version preRelease = Version.of("2.0.0-alpha");
            Version lowerVersion = Version.of("1.9.9");
            Version higherVersion = Version.of("2.0.1");

            Assertions.assertTrue(Version.isNewer(preRelease, lowerVersion));
            Assertions.assertTrue(Version.isOlder(preRelease, higherVersion));
        }
    }

    @Nested
    class FormattingTests {
        @Test
        public void testToString() {
            Version v1 = Version.of("1.2.3");
            Assertions.assertEquals("1.2.3", v1.toString());

            Version v2 = Version.of("1.2.3-alpha");
            Assertions.assertEquals("1.2.3-alpha", v2.toString());

            Version v3 = Version.of("1.2.3+build.1");
            Assertions.assertEquals("1.2.3+build.1", v3.toString());

            Version v4 = Version.of("1.2.3-alpha+build.1");
            Assertions.assertEquals("1.2.3-alpha+build.1", v4.toString());
        }
    }

    @Nested
    class CreationTests {
        @Test
        public void testCreateFromComponents() {
            Version v1 = Version.of(1, 2, 3);
            Assertions.assertEquals("1.2.3", v1.toString());

            Version v2 = Version.of(1, 2, 3, "alpha");
            Assertions.assertEquals("1.2.3-alpha", v2.toString());

            Version v3 = Version.of(1, 2, 3, null, "build.1");
            Assertions.assertEquals("1.2.3+build.1", v3.toString());

            Version v4 = Version.of(1, 2, 3, "alpha", "build.1");
            Assertions.assertEquals("1.2.3-alpha+build.1", v4.toString());
        }
    }

    @Nested
    class CommonPreReleasePatternTests {
        @Test
        public void testCommonPreReleasePatterns() {
            Version[] versions = new Version[]{
                Version.of("1.0.0-alpha"),
                Version.of("1.0.0-alpha.1"),
                Version.of("1.0.0-beta"),
                Version.of("1.0.0-beta.1"),
                Version.of("1.0.0-rc"),
                Version.of("1.0.0-rc.1"),
                Version.of("1.0.0")
            };

            for (int i = 0; i < versions.length - 1; i++) {
                Assertions.assertTrue(Version.isOlder(versions[i], versions[i + 1]));
            }
        }

        @Test
        public void testBuildNumberPreReleases() {
            Version build1 = Version.of("1.0.0-build.1");
            Version build2 = Version.of("1.0.0-build.2");
            Version build10 = Version.of("1.0.0-build.10");

            Assertions.assertTrue(Version.isOlder(build1, build2));
            Assertions.assertTrue(Version.isOlder(build2, build10));
        }
    }

    @Nested
    class StandardComplianceTests {
        @Test
        public void testStartWithV() {
            Assertions.assertDoesNotThrow(() -> Version.of("v1.0.0"));
        }

        @Test
        public void testNoLeadingZeros() {
            Assertions.assertThrows(VersionParseException.class, () -> Version.of("01.0.0"));
            Assertions.assertThrows(VersionParseException.class, () -> Version.of("1.00.0"));
            Assertions.assertThrows(VersionParseException.class, () -> Version.of("1.0.00"));
        }

        @Test
        public void testLeadingZerosInPreRelease() {
            Assertions.assertThrows(VersionParseException.class, () -> Version.of("1.0.0-01"));
        }
    }
}