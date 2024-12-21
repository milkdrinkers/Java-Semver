package io.github.milkdrinkers.javasemver.exception;

public class VersionException extends RuntimeException {
    public VersionException(String message) {
        super(message);
    }
    public VersionException(String message, Exception e) {
        super(message, e);
    }
}
