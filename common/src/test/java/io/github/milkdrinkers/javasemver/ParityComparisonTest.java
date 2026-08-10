package io.github.milkdrinkers.javasemver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class ParityComparisonTest {
    @ParameterizedTest(name = "[{index}] \"{0}\" < \"{1}\"")
    @MethodSource("pairs")
    void lowerIsLessThanHigher(String lower, String higher) {
        final Version lowerVersion = Version.parse(lower);
        final Version higherVersion = Version.parse(higher);

        Assertions.assertTrue(lowerVersion.isLessThan(higherVersion), () -> "expected \"" + lower + "\" to be less than \"" + higher + "\"");
        Assertions.assertTrue(higherVersion.isGreaterThan(lowerVersion), () -> "expected \"" + higher + "\" to be greater than \"" + lower + "\"");
    }

    static Stream<Arguments> pairs() {
        return Stream.of(
            Arguments.of("1.0.0-alpha", "1.0.0-alpha.1"),
            Arguments.of("1.0.0-alpha.1", "1.0.0-alpha.beta"),
            Arguments.of("1.0.0-alpha.beta", "1.0.0-beta"),
            Arguments.of("1.0.0-beta", "1.0.0-beta.2"),
            Arguments.of("1.0.0-beta.2", "1.0.0-beta.11"),
            Arguments.of("1.0.0-beta.11", "1.0.0-rc.1"),
            Arguments.of("1.0.0-rc.1", "1.0.0"),
            Arguments.of("1.0.0", "2.0.0"),
            Arguments.of("1.0.0-1", "1.0.0-alpha"),
            Arguments.of("1.0.0-2", "1.0.0-10"),
            Arguments.of("0.0.1", "0.0.2"),
            Arguments.of("0.1.0", "0.2.0"),
            Arguments.of("1.2.3", "1.2.4"),
            Arguments.of("1.2.3", "1.3.0"),
            Arguments.of("1.2.3", "2.0.0")
        );
    }
}
