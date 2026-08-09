package io.github.milkdrinkers.javasemver.exception;

public class VersionParseException extends VersionException {
    private static final long serialVersionUID = 5172097489090152114L;

    public VersionParseException(String message) {
        super(message);
    }

    public VersionParseException(String message, Exception e) {
        super(message, e);
    }
}
