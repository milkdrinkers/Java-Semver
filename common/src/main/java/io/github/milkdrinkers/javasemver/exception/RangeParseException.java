package io.github.milkdrinkers.javasemver.exception;

/**
 * Thrown when a version range cannot be parsed.
 */
public class RangeParseException extends VersionException {
    private static final long serialVersionUID = 5693929286274031511L;

    /**
     * Constructs a new range parse exception with the specified message.
     *
     * @param message the detail message
     */
    public RangeParseException(String message) {
        super(message);
    }

    /**
     * Constructs a new range parse exception with the specified message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public RangeParseException(String message, Exception cause) {
        super(message, cause);
    }
}