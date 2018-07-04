

package com.quanode.behaviors;


public interface BehaviourCallback<T> {

    void callback(T t, BehaviourError e);
}

