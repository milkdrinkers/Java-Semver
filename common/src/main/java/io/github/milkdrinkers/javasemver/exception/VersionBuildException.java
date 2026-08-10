package io.github.milkdrinkers.javasemver.exception;

public class VersionBuildException extends VersionException {
    private static final long serialVersionUID = 983442101168699796L;

    public VersionBuildException(String message) {
        super(message);
    }

    public VersionBuildException(String message, Exception e) {
        super(message, e);
    }
}
