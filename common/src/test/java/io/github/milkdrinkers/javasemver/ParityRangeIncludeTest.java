package io.github.milkdrinkers.javasemver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ParityRangeIncludeTest {
    @ParameterizedTest(name = "[{index}] \"{1}\" satisfies \"{0}\"")
    @CsvSource({
        "^1.2.3, 1.2.3",
        "^1.2.3, 1.9.9",
        "^0.2.3, 0.2.9",
        "~1.2.3, 1.2.3",
        "~1.2.3, 1.2.9",
        "1.2.3 - 2.3.4, 1.2.3",
        "1.2.3 - 2.3.4, 2.3.4",
        "1.2.3 - 2.3, 2.3.9",
        "1.2 - 2.3.4, 1.2.0",
        "1.x, 1.9.9",
        "1.2.x, 1.2.5",
        "*, 3.1.4",
        "1, 1.4.2",
        "1.2, 1.2.7",
        ">=1.0.0 <2.0.0, 1.5.0",
        "1.2.3 || >=2.0.0, 1.2.3",
        "1.2.3 || >=2.0.0, 2.5.0",
        ">=1.2.3-alpha <2.0.0, 1.2.3-alpha.4"
    })
    void satisfies(String range, String version) {
        Assertions.assertTrue(Version.parse(version).satisfies(range), () -> "expected \"" + version + "\" to satisfy \"" + range + "\"");
    }
}
