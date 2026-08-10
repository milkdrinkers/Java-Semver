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
            Version v = Version.parse("1.2.3");
            Assertions.assertEquals(1, v.getMajor());
            Assertions.assertEquals(2, v.getMinor());
            Assertions.assertEquals(3, v.getPatch());
            Assertions.assertEquals("", v.getPreRelease());
            Assertions.assertEquals("", v.getBuildMetadata());
        }

        @Test
        public void testParsingWithPreRelease() {
            Version v = Version.parse("1.2.3-alpha.1");
            Assertions.assertEquals(1, v.getMajor());
            Assertions.assertEquals(2, v.getMinor());
            Assertions.assertEquals(3, v.getPatch());
            Assertions.assertEquals("alpha.1", v.getPreRelease());
            Assertions.assertEquals("", v.getBuildMetadata());
        }

        @Test
        public void testParsingWithBuildMetadata() {
            Version v = Version.parse("1.2.3+build.20230101");
            Assertions.assertEquals(1, v.getMajor());
            Assertions.assertEquals(2, v.getMinor());
            Assertions.assertEquals(3, v.getPatch());
            Assertions.assertEquals("", v.getPreRelease());
            Assertions.assertEquals("build.20230101", v.getBuildMetadata());
        }

        @Test
        public void testParsingWithPreReleaseAndBuildMetadata() {
            Version v = Version.parse("1.2.3-alpha.1+build.20230101");
            Assertions.assertEquals(1, v.getMajor());
            Assertions.assertEquals(2, v.getMinor());
            Assertions.assertEquals(3, v.getPatch());
            Assertions.assertEquals("alpha.1", v.getPreRelease());
            Assertions.assertEquals("build.20230101", v.getBuildMetadata());
        }

        @Test
        public void testParsingWithLeadingZeros() {
            Assertions.assertThrows(VersionParseException.class, () -> Version.parse("1.02.03"));
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
            Assertions.assertThrows(VersionParseException.class, () -> Version.parse(version));
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
            Assertions.assertDoesNotThrow(() -> Version.parse(version));
        }
    }

    @Nested
    class ComparisonTests {
        @Test
        public void testReleaseVsPreRelease() {
            Version release = Version.parse("1.0.0");
            Version preRelease = Version.parse("1.0.0-alpha");

            Assertions.assertTrue(release.isGreaterThan(preRelease));
            Assertions.assertTrue(release.isAtLeast(preRelease));
            Assertions.assertFalse(release.isEqualTo(preRelease));
            Assertions.assertFalse(release.isAtMost(preRelease));
            Assertions.assertFalse(release.isLessThan(preRelease));
        }

        @Test
        public void testPreReleaseComparisons() {
            Version v1 = Version.parse("1.0.0-alpha");
            Version v2 = Version.parse("1.0.0-alpha.1");
            Version v3 = Version.parse("1.0.0-alpha.beta");
            Version v4 = Version.parse("1.0.0-beta");
            Version v5 = Version.parse("1.0.0-beta.2");
            Version v6 = Version.parse("1.0.0-beta.11");
            Version v7 = Version.parse("1.0.0-rc.1");

            Assertions.assertTrue(v1.isLessThan(v2));
            Assertions.assertTrue(v2.isLessThan(v3));
            Assertions.assertTrue(v3.isLessThan(v4));
            Assertions.assertTrue(v4.isLessThan(v5));
            Assertions.assertTrue(v5.isLessThan(v6));
            Assertions.assertTrue(v6.isLessThan(v7));
        }

        @Test
        public void testBuildMetadataIgnored() {
            Version v1 = Version.parse("1.0.0+build.1");
            Version v2 = Version.parse("1.0.0+build.2");

            Assertions.assertTrue(v1.isEqualTo(v2));
            Assertions.assertTrue(v1.isAtLeast(v2));
            Assertions.assertTrue(v1.isAtMost(v2));
            Assertions.assertFalse(v1.isGreaterThan(v2));
            Assertions.assertFalse(v1.isLessThan(v2));
        }

        @Test
        public void testPreReleaseWithBuildMetadata() {
            Version v1 = Version.parse("1.0.0-alpha+build.1");
            Version v2 = Version.parse("1.0.0-alpha+build.2");

            Assertions.assertTrue(v1.isEqualTo(v2));

            Version v3 = Version.parse("1.0.0-alpha.1+build.1");
            Assertions.assertTrue(v3.isGreaterThan(v1));
        }

        @Test
        public void testNumericVsAlphabeticPreRelease() {
            Version v1 = Version.parse("1.0.0-alpha.1");
            Version v2 = Version.parse("1.0.0-alpha.beta");

            Assertions.assertTrue(v1.isLessThan(v2));

            Version v3 = Version.parse("1.0.0-beta.2");
            Version v4 = Version.parse("1.0.0-beta.11");

            Assertions.assertTrue(v3.isLessThan(v4));
        }

        @ParameterizedTest
        @MethodSource("provideVersionPairs")
        public void testVersionOrder(String olderVersion, String newerVersion) {
            Version older = Version.parse(olderVersion);
            Version newer = Version.parse(newerVersion);

            Assertions.assertTrue(older.isLessThan(newer),
                olderVersion + " should be older than " + newerVersion);
            Assertions.assertTrue(newer.isGreaterThan(older),
                newerVersion + " should be newer than " + olderVersion);
            Assertions.assertFalse(older.isEqualTo(newer));
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
            Version v1 = Version.parse("1.2.3");
            Version v2 = Version.parse("1.2.3");

            Assertions.assertTrue(v1.isEqualTo(v2));
            Assertions.assertTrue(v1.isAtLeast(v2));
            Assertions.assertTrue(v1.isAtMost(v2));
            Assertions.assertFalse(v1.isGreaterThan(v2));
            Assertions.assertFalse(v1.isLessThan(v2));
        }

        @Test
        public void testVersionsWithDifferentBuildMetadataEqual() {
            Version v1 = Version.parse("1.2.3+build.1");
            Version v2 = Version.parse("1.2.3+build.2");

            Assertions.assertTrue(v1.isEqualTo(v2));
        }

        @Test
        public void testVersionsWithSamePreReleaseEqual() {
            Version v1 = Version.parse("1.2.3-alpha");
            Version v2 = Version.parse("1.2.3-alpha");

            Assertions.assertTrue(v1.isEqualTo(v2));
        }

        @Test
        public void testVersionsWithSamePreReleaseAndDifferentBuildMetadataEqual() {
            Version v1 = Version.parse("1.2.3-alpha+build.1");
            Version v2 = Version.parse("1.2.3-alpha+build.2");

            Assertions.assertTrue(v1.isEqualTo(v2));
        }
    }

    @Nested
    class EdgeCaseTests {
        @Test
        public void testZeroVersions() {
            Version v1 = Version.parse("0.0.0");
            Version v2 = Version.parse("0.0.1");

            Assertions.assertTrue(v1.isLessThan(v2));
        }

        @Test
        public void testVeryLargeNumbers() {
            Version v1 = Version.parse("999999.999999.999999");
            Version v2 = Version.parse("1000000.0.0");

            Assertions.assertTrue(v1.isAtMost(v2));
        }

        @Test
        public void testNumericPreReleaseIdentifiers() {
            Version v1 = Version.parse("1.0.0-1");
            Version v2 = Version.parse("1.0.0-2");

            Assertions.assertTrue(v1.isLessThan(v2));
        }

        @Test
        public void testAlphanumericPreReleaseIdentifiers() {
            Version v1 = Version.parse("1.0.0-a1");
            Version v2 = Version.parse("1.0.0-a2");

            Assertions.assertTrue(v1.isLessThan(v2));
        }

        @Test
        public void testDifferentLengthPreReleaseIdentifiers() {
            Version v1 = Version.parse("1.0.0-alpha");
            Version v2 = Version.parse("1.0.0-alpha.1");

            Assertions.assertTrue(v1.isLessThan(v2));
        }

        @Test
        public void testSpecialPreReleaseIdentifiers() {
            Version snapshot = Version.parse("2.0.0-snapshot");
            Version beta = Version.parse("2.0.0-beta.2");
            Version release = Version.parse("2.0.0");

            Assertions.assertTrue(snapshot.isGreaterThan(beta));
            Assertions.assertTrue(release.isGreaterThan(snapshot));
        }
    }

    @Nested
    class PreReleaseIdentifierOrderingTests {
        @Test
        public void testNumericIdentifiersComparedNumerically() {
            Version v1 = Version.parse("1.0.0-1");
            Version v2 = Version.parse("1.0.0-alpha");

            Assertions.assertTrue(v1.isLessThan(v2));

            Version v3 = Version.parse("1.0.0-2");
            Version v4 = Version.parse("1.0.0-10");

            Assertions.assertTrue(v3.isLessThan(v4));
        }

        @Test
        public void testIdentifiersWithLettersComparedLexically() {
            Version v1 = Version.parse("1.0.0-alpha");
            Version v2 = Version.parse("1.0.0-beta");

            Assertions.assertTrue(v1.isLessThan(v2));

            Version v3 = Version.parse("1.0.0-abc");
            Version v4 = Version.parse("1.0.0-abd");

            Assertions.assertTrue(v3.isLessThan(v4));
        }

        @Test
        public void testIdentifiersWithSamePrefixComparedByLength() {
            Version v1 = Version.parse("1.0.0-alpha");
            Version v2 = Version.parse("1.0.0-alpha.1");

            Assertions.assertTrue(v1.isLessThan(v2));

            Version v3 = Version.parse("1.0.0-alpha.beta");
            Version v4 = Version.parse("1.0.0-alpha.beta.1");

            Assertions.assertTrue(v3.isLessThan(v4));
        }
    }

    @Nested
    class ComplexComparisonTests {
        @Test
        public void testMixedVersionComparisons() {
            Version[] versions = new Version[]{
                Version.parse("1.0.0"),
                Version.parse("1.0.1"),
                Version.parse("1.1.0"),
                Version.parse("1.1.1"),
                Version.parse("2.0.0"),
                Version.parse("2.1.0")
            };

            for (int i = 0; i < versions.length - 1; i++) {
                for (int j = i + 1; j < versions.length; j++) {
                    Assertions.assertTrue(versions[i].isLessThan(versions[j]));
                    Assertions.assertTrue(versions[j].isGreaterThan(versions[i]));
                }
            }
        }

        @Test
        public void testPreReleaseVsReleaseForSameVersion() {
            Version[] versions = new Version[]{
                Version.parse("1.0.0-alpha"),
                Version.parse("1.0.0-alpha.1"),
                Version.parse("1.0.0-beta"),
                Version.parse("1.0.0-beta.2"),
                Version.parse("1.0.0-beta.11"),
                Version.parse("1.0.0-rc.1"),
                Version.parse("1.0.0")
            };

            for (int i = 0; i < versions.length - 1; i++) {
                for (int j = i + 1; j < versions.length; j++) {
                    Assertions.assertTrue(versions[i].isLessThan(versions[j]));
                    Assertions.assertTrue(versions[j].isGreaterThan(versions[i]));
                }
            }
        }

        @Test
        public void testPreReleaseVsHigherVersion() {
            Version preRelease = Version.parse("2.0.0-alpha");
            Version lowerVersion = Version.parse("1.9.9");
            Version higherVersion = Version.parse("2.0.1");

            Assertions.assertTrue(preRelease.isGreaterThan(lowerVersion));
            Assertions.assertTrue(preRelease.isLessThan(higherVersion));
        }
    }

    @Nested
    class FormattingTests {
        @Test
        public void testToString() {
            Version v1 = Version.parse("1.2.3");
            Assertions.assertEquals("1.2.3", v1.toString());

            Version v2 = Version.parse("1.2.3-alpha");
            Assertions.assertEquals("1.2.3-alpha", v2.toString());

            Version v3 = Version.parse("1.2.3+build.1");
            Assertions.assertEquals("1.2.3+build.1", v3.toString());

            Version v4 = Version.parse("1.2.3-alpha+build.1");
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
                Version.parse("1.0.0-alpha"),
                Version.parse("1.0.0-alpha.1"),
                Version.parse("1.0.0-beta"),
                Version.parse("1.0.0-beta.1"),
                Version.parse("1.0.0-rc"),
                Version.parse("1.0.0-rc.1"),
                Version.parse("1.0.0")
            };

            for (int i = 0; i < versions.length - 1; i++) {
                Assertions.assertTrue(versions[i].isLessThan(versions[i + 1]));
            }
        }

        @Test
        public void testBuildNumberPreReleases() {
            Version build1 = Version.parse("1.0.0-build.1");
            Version build2 = Version.parse("1.0.0-build.2");
            Version build10 = Version.parse("1.0.0-build.10");

            Assertions.assertTrue(build1.isLessThan(build2));
            Assertions.assertTrue(build2.isLessThan(build10));
        }
    }

    @Nested
    class StandardComplianceTests {
        @Test
        public void testStartWithV() {
            Assertions.assertDoesNotThrow(() -> Version.parse("v1.0.0"));
        }

        @Test
        public void testNoLeadingZeros() {
            Assertions.assertThrows(VersionParseException.class, () -> Version.parse("01.0.0"));
            Assertions.assertThrows(VersionParseException.class, () -> Version.parse("1.00.0"));
            Assertions.assertThrows(VersionParseException.class, () -> Version.parse("1.0.00"));
        }

        @Test
        public void testLeadingZerosInPreRelease() {
            Assertions.assertThrows(VersionParseException.class, () -> Version.parse("1.0.0-01"));
        }
    }
}