package io.github.milkdrinkers.javasemver.internal;

import io.github.milkdrinkers.javasemver.Version;
import io.github.milkdrinkers.javasemver.exception.VersionParseException;
import org.jetbrains.annotations.ApiStatus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Strict semantic version parsing logic used by {@link Version}.
 */
@ApiStatus.Internal
public final class VersionParsing {
    private static final Pattern SEMVER_REGEX = Pattern.compile(
        "^(?<major>0|[1-9]\\d*)\\.(?<minor>0|[1-9]\\d*)\\.(?<patch>0|[1-9]\\d*)" +
            "(?:-(?<prerelease>(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)" +
            "(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?" +
            "(?:\\+(?<meta>[0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$");

    private static final Pattern LOOSE_SEMVER_REGEX = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)(?:-([0-9A-Za-z-.]+))?(?:\\+([0-9A-Za-z-.]+))?$");

    private VersionParsing() {
    }

    public static Version parseStrict(String input) {
        if (input == null)
            throw new VersionParseException("Version string was null.");

        String s = input.trim();

        if (s.length() >= 1 && (s.charAt(0) == 'v' || s.charAt(0) == 'V'))
            s = s.substring(1);

        final Matcher m = SEMVER_REGEX.matcher(s);
        if (!m.matches())
            throw new VersionParseException(String.format("Version could not be parsed from version string \"%s\".", input));

        final String pre = m.group("prerelease");
        final String meta = m.group("meta");

        try {
            return Version.of(
                Long.parseLong(m.group("major")),
                Long.parseLong(m.group("minor")),
                Long.parseLong(m.group("patch")),
                pre == null ? "" : pre,
                meta == null ? "" : meta
            );
        } catch (NumberFormatException e) {
            throw new VersionParseException(String.format("Version could not be parsed from version string \"%s\".", input), e);
        }
    }

    public static Version parseLoose(String input) {
        if (input == null)
            throw new VersionParseException("Version string was null.");

        String s = input.trim();

        if (s.length() >= 1 && s.charAt(0) == '=')
            s = s.substring(1);

        if (s.length() >= 1 && (s.charAt(0) == 'v' || s.charAt(0) == 'V'))
            s = s.substring(1);

        final Matcher m = LOOSE_SEMVER_REGEX.matcher(s);
        if (!m.matches())
            throw new VersionParseException(String.format("Version could not be parsed from version string \"%s\".", input));

        final String pre = m.group(4);
        final String meta = m.group(5);

        try {
            return Version.of(
                Long.parseLong(m.group(1)),
                Long.parseLong(m.group(2)),
                Long.parseLong(m.group(3)),
                pre == null ? "" : pre,
                meta == null ? "" : meta
            );
        } catch (NumberFormatException e) {
            throw new VersionParseException(String.format("Version could not be parsed from version string \"%s\".", input), e);
        }
    }
}
