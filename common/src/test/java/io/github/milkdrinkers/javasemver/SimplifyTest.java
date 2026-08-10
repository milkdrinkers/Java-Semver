package io.github.milkdrinkers.javasemver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class SimplifyTest {
    @Test
    void collapsesContiguousRun() {
        List<Version> versions = Arrays.asList(
            Version.parse("1.0.0"),
            Version.parse("1.1.0"),
            Version.parse("1.2.0"),
            Version.parse("1.3.0"),
            Version.parse("1.4.0")
        );
        Range simplified = Range.simplify(versions, Range.parse(">=1.0.0 <2.0.0"));
        for (Version v : versions) {
            Assertions.assertEquals(Range.parse(">=1.0.0 <2.0.0").contains(v), simplified.contains(v), "membership must be preserved for " + v);
        }
    }

    @Test
    void equivalenceOverTheSet() {
        List<Version> versions = Arrays.asList(
            Version.parse("0.9.0"),
            Version.parse("1.0.0"),
            Version.parse("1.5.0"),
            Version.parse("2.0.0")
        );
        Range input = Range.parse(">=1.0.0 <2.0.0");
        Range simplified = Range.simplify(versions, input);
        for (Version v : versions) {
            Assertions.assertEquals(input.contains(v), simplified.contains(v));
        }
    }
}
