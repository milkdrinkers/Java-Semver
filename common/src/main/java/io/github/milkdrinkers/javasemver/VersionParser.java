package io.github.milkdrinkers.javasemver;

import io.github.milkdrinkers.javasemver.exception.VersionBuildException;
import io.github.milkdrinkers.javasemver.exception.VersionParseException;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains parsing logic for {@link Version} objects.
 */
public abstract class VersionParser {
    private final static Pattern SEMVER_REGEX = Pattern.compile("^(?<major>0|[1-9]\\d*)\\.(?<minor>0|[1-9]\\d*)\\.(?<patch>0|[1-9]\\d*)(?:-(?<prerelease>(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+(?<meta>[0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$"); // Extract named fields for semantic version

    /**
     * Parse a {@link String} into a {@link Version}.
     *
     * @param unparsedVersion the unparsed version string
     * @return the resulting {@link Version}
     * @throws VersionParseException thrown if a valid semantic version could not be parsed from the string
     * @apiNote Uses {@link VersionBuilder} internally
     * @implNote Preceding "V" or "v" characters are stripped from the unparsedVersion
     */
    public static Version parse(String unparsedVersion) throws VersionParseException {
        // Check if version meets minimal requirements (if it doesn't have 4+ characters it can't be valid semver)
        if (unparsedVersion.length() < 5)
            throw new VersionParseException(String.format("Version could not be parsed from version string \"%s\".", unparsedVersion));

        // Strip leading "V" before version
        if (unparsedVersion.toUpperCase().startsWith("V"))
            unparsedVersion = unparsedVersion.substring(1);

        try {
            // Grab version details from string
            final Matcher matcher = SEMVER_REGEX.matcher(unparsedVersion);
            if (!matcher.matches())
                throw new VersionParseException(String.format("Version could not be parsed from version string \"%s\".", unparsedVersion));

            // Grap values from matcher groups and put in optionals
            final Optional<Long> major = Optional.ofNullable(matcher.group("major")).map(Long::parseLong);
            final Optional<Long> minor = Optional.ofNullable(matcher.group("minor")).map(Long::parseLong);
            final Optional<Long> patch = Optional.ofNullable(matcher.group("patch")).map(Long::parseLong);
            final Optional<String> preRelease = Optional.ofNullable(matcher.group("prerelease"));
            final Optional<String> meta = Optional.ofNullable(matcher.group("meta"));

            // Check for missing data
            if (!major.isPresent())
                throw new VersionParseException(String.format("Major version could not be parsed from version string \"%s\" when constructing Version object.", unparsedVersion));

            if (!minor.isPresent())
                throw new VersionParseException(String.format("Minor version could not be parsed from version string \"%s\" when constructing Version object.", unparsedVersion));

            if (!patch.isPresent())
                throw new VersionParseException(String.format("Patch version could not be parsed from version string \"%s\" when constructing Version object.", unparsedVersion));

            return new VersionBuilder()
                .withMajor(major.get())
                .withMinor(minor.get())
                .withPatch(patch.get())
                .withPreRelease(preRelease.orElse(""))
                .withMeta(meta.orElse(""))
                .build();
        } catch (IllegalStateException e) {
            throw new VersionParseException(String.format("Match operation failed parsing version from string \"%s\" when constructing Version object.", unparsedVersion));
        } catch (IllegalArgumentException e) {
            throw new VersionParseException(String.format("Regex groups were not found while parsing version from string \"%s\" when constructing Version object.", unparsedVersion));
        } catch (VersionBuildException e) {
            throw new VersionParseException(String.format("Builder failed while parsing version from string \"%s\" when constructing Version object.", unparsedVersion), e);
        }
    }
}
