package io.github.oitstack.goblin.runtime.exceptions;

public class RuntimeLaunchException extends RuntimeException {
    public RuntimeLaunchException(String message) {
        super(message);
    }

    public RuntimeLaunchException(String message, Exception exception) {
        super(message, exception);
    }

    public RuntimeLaunchException(Exception e) {
        super(e);
    }
}
