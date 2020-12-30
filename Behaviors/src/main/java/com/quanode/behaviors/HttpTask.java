package com.quanode.behaviors;

import org.json.JSONObject;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HttpTask {

    public String baseUrl;

    public HttpTask(String baseUrl) {

        this.baseUrl = baseUrl;
    }
    public static GETURLFunction _getURL_;
    public static HttpURLConnection getConnection(String path, Map<String, Object> headers, String method, Map<String,
            Object> body) throws Exception {

        URL url;
        url = _getURL_.apply(path);
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setRequestMethod(method);
        if (headers == null) headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        if (!headers.isEmpty()) {

            for (String key : headers.keySet()) {

                httpCon.setRequestProperty(key, headers.get(key).toString());
            }
        }
        if (body != null && !body.isEmpty()) {

            httpCon.setDoOutput(true);
            OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
            JSONObject json = new JSONObject(body);
            out.write(json.toString());
            out.close();
        }
        return httpCon;
    }
}
