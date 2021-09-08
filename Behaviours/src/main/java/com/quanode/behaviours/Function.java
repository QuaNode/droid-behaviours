
package com.quanode.behaviours;

public interface Function<F, L, G> {

    G apply(F var1, L var2) throws Exception;
    boolean equals(Object var1, Object var2);
}
