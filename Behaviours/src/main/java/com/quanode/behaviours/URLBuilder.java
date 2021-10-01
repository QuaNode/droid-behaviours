/**
 * Created by Ahmed on 8/21/17.
 */

package com.quanode.behaviours;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public interface URLBuilder {

    URL concat(String path) throws IOException, URISyntaxException;
    URL split(String split, String path) throws IOException, URISyntaxException;
}
