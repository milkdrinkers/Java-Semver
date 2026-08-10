package io.github.milkdrinkers.javasemver.internal;

import io.github.milkdrinkers.javasemver.Version;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@code Coerce}/{@code Clean} logic used by {@link Version}.
 */
@ApiStatus.Internal
public final class Coercion {
    private static final Pattern COERCE_REGEX = Pattern.compile("(^|[^\\d])(\\d{1,16})(?:\\.(\\d{1,16}))?(?:\\.(\\d{1,16}))?(?:$|[^\\d])"); // up to three digit groups of at most 16 digits each, each delimited by a non digit or a string boundary (so over long numbers are skipped)

    private Coercion() {
    }

    /**
     * Scans an arbitrary string for the first thing that looks like a version and coerces it
     * into a {@link Version}, defaulting any missing minor/patch component to {@code 0} and
     * discarding any trailing pre release/build metadata data.
     *
     * @param input the string to scan
     * @return the coerced version, or {@link Optional#empty()} if no version like data was found
     * @apiNote The extracted {@code major.minor.patch} is parsed strictly, so an extracted component with leading zeros (e.g. {@code 01.02.03}) yields an empty result, and each component is capped at 16 digits.
     */
    public static @NotNull Optional<Version> coerce(String input) {
        if (input == null)
            return Optional.empty();

        final Matcher m = COERCE_REGEX.matcher(input);
        if (!m.find())
            return Optional.empty();

        final String major = m.group(2);
        final String minor = m.group(3) != null ? m.group(3) : "0";
        final String patch = m.group(4) != null ? m.group(4) : "0";

        return Version.parseOptional(major + "." + minor + "." + patch);
    }

    /**
     * Normalizes a loosely formatted version string into a strict semantic version string, tolerating surrounding whitespace, a leading {@code =} and a leading {@code v}/{@code V}.
     *
     * @param input the string to clean
     * @return the cleaned version string, or {@link Optional#empty()} if the remainder was not a valid version
     */
    public static @NotNull Optional<String> clean(String input) {
        if (input == null)
            return Optional.empty();

        // trip leading run of =/v characters
        final String s = input.trim().replaceFirst("^[=vV]+", "");

        return Version.parseOptional(s).map(Version::toString);
    }
}
