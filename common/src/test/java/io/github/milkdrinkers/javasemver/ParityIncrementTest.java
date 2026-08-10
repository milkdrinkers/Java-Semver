package io.github.milkdrinkers.javasemver;

import io.github.milkdrinkers.javasemver.enums.ReleaseType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class ParityIncrementTest {
    @ParameterizedTest(name = "[{index}] \"{0}\".increment({1}) = \"{2}\"")
    @MethodSource("noIdentifierCases")
    void incrementWithoutIdentifier(String input, ReleaseType release, String expected) {
        Assertions.assertEquals(expected, Version.parse(input).increment(release).toString());
    }

    @ParameterizedTest(name = "[{index}] \"{0}\".increment({1}, \"{2}\") = \"{3}\"")
    @MethodSource("withIdentifierCases")
    void incrementWithIdentifier(String input, ReleaseType release, String identifier, String expected) {
        Assertions.assertEquals(expected, Version.parse(input).increment(release, identifier).toString());
    }

    static Stream<Arguments> noIdentifierCases() {
        return Stream.of(
            Arguments.of("1.2.3", ReleaseType.MAJOR, "2.0.0"),
            Arguments.of("1.2.3", ReleaseType.MINOR, "1.3.0"),
            Arguments.of("1.2.3", ReleaseType.PATCH, "1.2.4"),
            Arguments.of("1.2.3", ReleaseType.PREMAJOR, "2.0.0-0"),
            Arguments.of("1.2.3", ReleaseType.PREMINOR, "1.3.0-0"),
            Arguments.of("1.2.3", ReleaseType.PREPATCH, "1.2.4-0"),
            Arguments.of("1.2.3", ReleaseType.PRERELEASE, "1.2.4-0"),
            Arguments.of("1.2.3-alpha.1", ReleaseType.PRERELEASE, "1.2.3-alpha.2"),
            Arguments.of("2.0.0-alpha.1", ReleaseType.MAJOR, "2.0.0")
        );
    }

    static Stream<Arguments> withIdentifierCases() {
        return Stream.of(
            Arguments.of("1.2.3", ReleaseType.PREPATCH, "alpha", "1.2.4-alpha.0"),
            Arguments.of("1.2.3-alpha.1", ReleaseType.PRERELEASE, "alpha", "1.2.3-alpha.2")
        );
    }
}
