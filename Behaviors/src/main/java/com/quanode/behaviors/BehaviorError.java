package com.quanode.behaviors;


public class BehaviorError extends Error {

    int code;
    String message;

    public BehaviorError(String message) {
        super(message);
        this.message = message;
    }

    public int getErrorCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
