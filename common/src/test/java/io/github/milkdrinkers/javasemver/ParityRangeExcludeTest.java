package io.github.milkdrinkers.javasemver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ParityRangeExcludeTest {
    @ParameterizedTest(name = "[{index}] \"{1}\" does not satisfy \"{0}\"")
    @CsvSource({
        "^1.2.3, 2.0.0",
        "^1.2.3, 1.2.2",
        "^0.2.3, 0.3.0",
        "~1.2.3, 1.3.0",
        "~1.2.3, 1.2.2",
        "1.2.3 - 2.3.4, 2.3.5",
        "1.2.3 - 2.3.4, 1.2.2",
        "1.2.3 - 2.3, 2.4.0",
        "1.2 - 2.3.4, 1.1.9",
        "1.x, 2.0.0",
        "1.2.x, 1.3.0",
        "1.2.x, 1.1.9",
        ">=1.2.3 <2.0.0, 2.0.0",
        ">=1.2.3 <2.0.0, 1.0.0",
        "1.2.3 || >=2.0.0, 1.5.0",
        ">=1.0.0 <2.0.0, 1.2.3-alpha.4"
    })
    void doesNotSatisfy(String range, String version) {
        Assertions.assertFalse(Version.parse(version).satisfies(range), () -> "expected \"" + version + "\" NOT to satisfy \"" + range + "\"");
    }
}
