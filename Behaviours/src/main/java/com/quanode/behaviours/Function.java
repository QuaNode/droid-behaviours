package com.quanode.behaviours;

public interface Function<F, L, G> {

    G call(F var1, L var2) throws Exception;

    interface Void<F> {

        void call(F var) throws Exception;
    }
}