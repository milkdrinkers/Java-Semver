package io.github.milkdrinkers.javasemver.exception;

public class VersionParseException extends VersionException {
    public VersionParseException(String message) {
        super(message);
    }
    public VersionParseException(String message, Exception e) {
        super(message, e);
    }
}
