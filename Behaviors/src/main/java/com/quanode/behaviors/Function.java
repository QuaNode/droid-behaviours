
package com.quanode.behaviors;

public interface Function<F, L, G> {

    G apply(F var1, L var2) throws Exception;
    boolean equals(Object var1, Object var2);
}
