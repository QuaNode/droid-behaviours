package com.quanode.behaviours;

import android.os.AsyncTask;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HttpTask {

    public GETURLFunction _getURL_;

    public HttpTask(GETURLFunction getURL) {

        _getURL_ = getURL;
    }

    public AsyncTask<Object, Void, BehaviourCallback> execute (Object... params) {

        String path = (String) params[0];
        Map<String, Object> headers = (Map<String, Object>) params[1];
        String method = (String) params[2];
        Map<String, Object> body = (Map<String, Object>) params[3];
        BehaviourCallback cb = (BehaviourCallback) params[4];
        URL url;
        try {

            url = _getURL_.apply(path);
        } catch (Exception ex) {

            ex.printStackTrace();
            BehaviourError error = new BehaviourError(ex.getMessage(), -1, ex);
            cb.callback(null, error);
            return null;
        }
        HttpRequest connection = new HttpRequest();
        connection.execute(url, headers, method, body, cb);
        return connection;
    }

    private static HttpURLConnection getConnection(URL url, Map<String, Object> headers, String method,
                                            Map<String, Object> body) throws Exception {

        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.setRequestMethod(method);
        if (headers == null) headers = new HashMap<>();
        if (headers.get("Content-Type") == null)
            headers.put("Content-Type", "application/json");
        if (!headers.isEmpty()) {

            for (String key : headers.keySet()) {

                httpConnection.setRequestProperty(key, headers.get(key).toString());
            }
        }
        if (body != null && !body.isEmpty()) {

            httpConnection.setDoOutput(true);
            OutputStreamWriter out = new OutputStreamWriter(httpConnection.getOutputStream());
            JSONObject json = new JSONObject(body);
            out.write(json.toString());
            out.close();
        }
        return httpConnection;
    }

    private static class HttpRequest extends AsyncTask<Object, Void, BehaviourCallback> {

        HashMap<String, Object> response = null;
        BehaviourError error;
        Exception exception;

        @Override
        protected BehaviourCallback doInBackground(Object... params) {

            HttpURLConnection httpConnection = null;;
            BehaviourCallback cb = (BehaviourCallback) params[4];
            try {

                URL url = (URL) params[0];
                Map<String, Object> reqHeaders = (Map<String, Object>) params[1];
                String reqMethod = (String) params[2];
                Map<String, Object> reqBody = (Map<String, Object>) params[3];
                httpConnection = getConnection(url, reqHeaders, reqMethod, reqBody);
                InputStream in = httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK ?
                        httpConnection.getInputStream() : httpConnection.getErrorStream();
                StringBuilder sb = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String read;
                while ((read = br.readLine()) != null) {

                    sb.append(read);
                }
                br.close();
                response = new HashMap<>();
                response.put("response", JSONMap.jsonToMap(sb.toString()));
                HashMap<String, String> headers = new HashMap<>();
                for (String key : httpConnection.getHeaderFields().keySet()) {

                    headers.put(key, httpConnection.getHeaderField(key));
                }
                response.put("headers", headers);
                if (httpConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {

                    exception = new Exception(httpConnection.getResponseMessage());
                    String errorMessage = exception.getMessage();
                    if (response.get("response") instanceof Map &&
                            ((Map) response.get("response")).get("message") != null)
                        errorMessage = (String) ((Map) response.get("response")).get("message");
                    error = new BehaviourError(errorMessage, httpConnection.getResponseCode(), exception);
                }
            } catch (Exception ex) {

                exception = ex;
                ex.printStackTrace();
            } finally {

                if (httpConnection != null) httpConnection.disconnect();
            }
            return cb;
        }

        @Override
        protected void onPostExecute(BehaviourCallback cb) {

            super.onPostExecute(cb);
            if (error == null && exception != null)
                error = new BehaviourError(exception.getMessage(), -1, exception);
            cb.callback(response, error);
        }
    }
}
