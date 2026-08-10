package io.github.milkdrinkers.javasemver;

import io.github.milkdrinkers.javasemver.enums.ReleaseType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Test class running tests ensuring full project parity with <a href="https://github.com/npm/node-semver">node-semver</a> 7.8.5.
 * A result file is generated from node-semver's tests and stored in test resources as {@code node_parity.txt}, and this test runs the same tests with java-semver and fails on mismatch in results.
 */
class NodeParityTest {
    @Test
    void matchesNodeSemver() throws Exception {
        final List<String> mismatches = new ArrayList<>();
        int checked = 0;

        try (InputStream in = getClass().getResourceAsStream("/node_parity.txt")) {
            Assertions.assertNotNull(in, "test resource file /node_parity.txt not found on the test classpath!");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isEmpty())
                        continue;

                    final String[] parts = line.split("\t", -1);
                    final String method = parts[0];
                    final String a = parts[1];
                    final String b = parts[2];
                    final String expected = parts[3];

                    checked++;
                    String actual;
                    try {
                        actual = compute(method, a, b);
                    } catch (RuntimeException e) {
                        actual = "THREW:" + e.getClass().getSimpleName();
                    }

                    if (!expected.equals(actual))
                        mismatches.add(method + "(" + a + (b.isEmpty() ? "" : " , " + b) + ") expected=" + expected + " actual=" + actual);
                }
            }
        }

        if (!mismatches.isEmpty()) {
            final StringBuilder sb = new StringBuilder();
            sb.append(mismatches.size()).append(" of ").append(checked).append(" node parity checks deviated:\n");
            for (final String m : mismatches)
                sb.append("  ").append(m).append('\n');
            Assertions.fail(sb.toString());
        }
    }

    private static String compute(String method, String a, String b) {
        return switch (method) {
            case "satisfies" -> String.valueOf(Version.parse(a).satisfies(b));
            case "gtr" -> String.valueOf(Version.parse(a).isAbove(Range.parse(b)));
            case "ltr" -> String.valueOf(Version.parse(a).isBelow(Range.parse(b)));
            case "intersects" -> String.valueOf(Range.parse(a).intersects(Range.parse(b)));
            case "compare" -> String.valueOf(Integer.signum(Version.parse(a).compareTo(Version.parse(b))));
            case "diff" -> Version.parse(a).difference(Version.parse(b)).map(rt -> rt.name().toLowerCase()).orElse("NULL");
            case "inc" -> Version.parse(a).increment(ReleaseType.valueOf(b.toUpperCase())).toString();
            case "coerce" -> Version.coerce(a).map(Version::toString).orElse("NULL");
            default -> throw new IllegalArgumentException("unknown method: " + method);
        };
    }
}
