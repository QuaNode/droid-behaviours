

package com.quanode.behaviors;


public interface BehaviorCallback<T> {

    void callback(T t, BehaviorError e);
}

