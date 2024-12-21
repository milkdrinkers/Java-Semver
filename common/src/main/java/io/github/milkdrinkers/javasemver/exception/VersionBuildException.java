package io.github.milkdrinkers.javasemver.exception;

public class VersionBuildException extends VersionException {
    public VersionBuildException(String message) {
        super(message);
    }
    public VersionBuildException(String message, Exception e) {
        super(message, e);
    }
}
