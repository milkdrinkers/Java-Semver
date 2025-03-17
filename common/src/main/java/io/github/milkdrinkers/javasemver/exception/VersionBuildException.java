package io.github.milkdrinkers.javasemver.exception;

public class VersionBuildException extends VersionException {
    private static final long serialVersionUID = 6995079221117712762L;

    public VersionBuildException(String message) {
        super(message);
    }

    public VersionBuildException(String message, Exception e) {
        super(message, e);
    }
}
