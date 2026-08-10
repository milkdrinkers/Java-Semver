package io.github.milkdrinkers.javasemver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RangeTildeCaretTest {
    @Test
    void tilde() {
        Range r = Range.parse("~1.2.3");
        Assertions.assertTrue(r.contains(Version.parse("1.2.9")));
        Assertions.assertFalse(r.contains(Version.parse("1.3.0")));
    }

    @Test
    void caretAboveZero() {
        Range r = Range.parse("^1.2.3");
        Assertions.assertTrue(r.contains(Version.parse("1.9.0")));
        Assertions.assertFalse(r.contains(Version.parse("2.0.0")));
    }

    @Test
    void caretZeroMinor() {
        Range r = Range.parse("^0.2.3");
        Assertions.assertTrue(r.contains(Version.parse("0.2.9")));
        Assertions.assertFalse(r.contains(Version.parse("0.3.0")));
    }
}
