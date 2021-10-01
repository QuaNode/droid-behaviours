package com.quanode.behaviours;

public interface BehaviourCallback<T> {

    void call(T t, BehaviourError e);
}
