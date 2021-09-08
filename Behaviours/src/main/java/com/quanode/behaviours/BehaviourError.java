package com.quanode.behaviours;


public class BehaviourError extends Error {

    int code;
    String message;
    private Exception exception;

    public BehaviourError(String message, int code, Exception exception) {
        super(message);
        this.code = code;
        this.exception = exception;
    }

    public int getErrorCode() {

        return code;
    }

    public String getMessage() {
        return message;
    }

    public Exception getException() {

        return exception;
    }
}
