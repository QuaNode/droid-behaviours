package com.quanode.behaviours;

public interface BehaviourCallback<T> {

    void callback(T t, BehaviourError e);
}
