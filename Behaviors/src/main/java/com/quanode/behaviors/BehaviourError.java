package com.quanode.behaviors;


public class BehaviourError extends Error {

    int code;
    String message;

    public BehaviourError(String message) {
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
