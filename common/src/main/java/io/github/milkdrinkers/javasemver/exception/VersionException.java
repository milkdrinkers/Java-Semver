package io.github.milkdrinkers.javasemver.exception;

public class VersionException extends RuntimeException {
    private static final long serialVersionUID = 2025937154918639292L;

    public VersionException(String message) {
        super(message);
    }

    public VersionException(String message, Exception e) {
        super(message, e);
    }
}
