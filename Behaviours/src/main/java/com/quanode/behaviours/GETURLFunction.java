/**
 * Created by Ahmed on 8/21/17.
 */

package com.quanode.behaviours;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public interface GETURLFunction {

    URL apply(String path) throws IOException, URISyntaxException;
}
