package io.github.oitstack.goblin.runtime.exceptions;

public class TimeoutException extends RuntimeException {
    public TimeoutException( String message) {
        super(message);
    }

    public TimeoutException( String message,  Exception exception) {
        super(message, exception);
    }

    public TimeoutException( Exception e) {
        super(e);
    }
}
