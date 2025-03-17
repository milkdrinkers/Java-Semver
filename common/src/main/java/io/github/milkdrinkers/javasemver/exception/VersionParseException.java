package io.github.milkdrinkers.javasemver.exception;

public class VersionParseException extends VersionException {
    private static final long serialVersionUID = 3454425882326722240L;
    public VersionParseException(String message) {
        super(message);
    }
    public VersionParseException(String message, Exception e) {
        super(message, e);
    }
}
