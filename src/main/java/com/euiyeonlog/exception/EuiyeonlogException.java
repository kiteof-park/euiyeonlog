package com.euiyeonlog.exception;

public abstract class EuiyeonlogException extends RuntimeException {

    public EuiyeonlogException(String message) {
        super(message);
    }

    public EuiyeonlogException(String message, Throwable cause) {
        super(message, cause);
    }

    public abstract int getStatusCode();
}


