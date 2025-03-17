package io.github.milkdrinkers.javasemver.exception;

public class VersionException extends RuntimeException {
    private static final long serialVersionUID = 5869428421273062603L;

    public VersionException(String message) {
        super(message);
    }

    public VersionException(String message, Exception e) {
        super(message, e);
    }
}
