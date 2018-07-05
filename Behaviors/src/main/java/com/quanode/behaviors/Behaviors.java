
package com.quanode.behaviors;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class Behaviors {

    Map<String, Object> behavioursJSON = null;
    Map<String, Object> parameters;
    Context context;
    GETURLFunction getURL = null;

    protected Behaviors(GETURLFunction getURL) {
        this.getURL = getURL;
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public Behaviors(final String baseUrl, Map<String, Object> defaults, Context context, ExceptionCallback cb) {

        this.context = context;
        parameters = getDataFromSharedPreference();
        if (defaults != null) parameters.putAll(defaults);
        this.getURL = new GETURLFunction() {

            @Override
            public URL apply(String path) throws MalformedURLException, URISyntaxException {

                URL url = new URL(baseUrl + path);
                url.toURI();
                return url;
            }

            @Override
            public boolean equals(Object var1) {

                return false;
            }
        };
        initiateBehaviour(cb);
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    private void initiateBehaviour(final ExceptionCallback cb) {

        ConnectionEstablishment connection = new ConnectionEstablishment();
        connection.execute("/behaviours", null, "GET", null, new Callback<Map<String, Object>>() {

            @Override
            public void callback(Map<String, Object> o, Exception e) {

                if (e != null) {

                    if (cb != null) cb.callback(e);
                } else {

                    if (o == null) {

                        if (cb != null)
                            cb.callback(new Exception("Failed to initialize Behaviors"));
                        return;
                    }
                    behavioursJSON = (Map<String, Object>) o.get("response");
                }
            }
        });
    }

    private Object getValueForParameter(Map<String, Object> parameter, Map<String, Object> data, String key, String name) throws Exception {

        if (data.get(key) != null) return data.get(key);
        if (parameter.get("value") != null) return parameter.get("value") instanceof Function ?
                (((Function<String, Map<String, Object>, String>) parameter.get("value"))).apply(name, data) : (String) parameter.get("value");
        else if (isEqual(parameter.get("source"), true)) {

            if (getDataFromSharedPreference().get(key) instanceof HashMap) {

                Map<String, Object> param = (Map<String, Object>) getDataFromSharedPreference().get(key);
                if (param.get("value") != null) return param.get("value");
            }
        }
        return null;
    }


    public Function<Map<String, Object>, BehaviorCallback<Map<String, Object>>, Void> getBehaviour(final String behaviourName)
            throws Exception {

        if (behaviourName == null) {

            throw new Exception("Invalid behaviour name");
        }
        if (behavioursJSON == null) {

            throw new Exception("Behaviors is not ready yet");
        }
        final Map<String, Object> behaviour = (Map<String, Object>) behavioursJSON.get(behaviourName);
        if (behaviour == null) {

            throw new Exception("This behaviour does not exist");
        }
        return new Function<Map<String, Object>, BehaviorCallback<Map<String, Object>>, Void>() {

            @Override
            public Void apply(Map<String, Object> data, final BehaviorCallback<Map<String, Object>> cb) throws Exception {

                if (data == null) {

                    data = new HashMap<String, Object>();
                }
                final Map<String, Object> params = new HashMap<String, Object>();

                if (behaviour.get("parameters") instanceof HashMap) {

                    params.putAll((HashMap<String, Object>) behaviour.get("parameters"));
                }
                params.putAll(parameters);
                Map<String, Object> headers = new HashMap<>();
                Map<String, Object> body = new HashMap<>();
                String url = (String) behaviour.get("path");
                for (final String key : params.keySet()) {

                    if (getValueForParameter((Map) params.get(key), data, key, behaviourName) == null)
                        continue;
                    if (params.get(key) instanceof HashMap) {

                        Object _unless_ = ((HashMap) params.get(key)).get("unless");
                        if (_unless_ instanceof String[]) {

                            if ((new ArrayList<String>((Collection<? extends String>) _unless_).contains(behaviourName))) {

                                continue;
                            }
                        }
                    }
                    if (params.get(key) instanceof Map) {

                        Object _for_ = ((Map) params.get(key)).get("for");
                        if (_for_ instanceof String[]) {

                            if (!(new ArrayList<String>((Collection<? extends String>) _for_).contains(behaviourName))) {

                                continue;
                            }
                        }
                    }
                    if (((Map<String, Object>) params.get(key)).get("type") != null) {

                        switch ((String) ((Map<String, Object>) params.get(key)).get("type")) {

                            case "header": {

                                headers.put((String) ((Map) params.get(key)).get("key"), getValueForParameter((Map) params.get(key), data, key, behaviourName));
                                break;
                            }
                            case "body": {

                                String[] paths = ((String) ((Map) params.get(key)).get("key")).split("\\.");
                                Map<String, Object> nestedData = body;
                                String lastPath = null;
                                for (String path : paths) {

                                    if (lastPath != null) {

                                        nestedData = (Map<String, Object>) nestedData.get(lastPath);
                                    }
                                    if (nestedData.get(path) == null) {

                                        nestedData.put(path, new HashMap<String, Object>());
                                    }
                                    lastPath = path;
                                }
                                if (lastPath != null)
                                    nestedData.put(lastPath, getValueForParameter((Map) params.get(key), data, key, behaviourName));
                                break;
                            }
                            case "query": {

                                if (url.indexOf('?') == -1) {

                                    url += '?';
                                }
                                String behaviourKey = URLEncoder.encode((String) ((HashMap) params.get(key)).get("key"), "UTF-8");
                                String dataValue = URLEncoder.encode(getValueForParameter((Map) params.get(key), data, key, behaviourName).toString(), "UTF-8");
                                url += '&' + behaviourKey + '=' + dataValue;
                                break;
                            }
                            case "path": {

                                String dataValue = URLEncoder.encode(getValueForParameter((Map) params.get(key), data, key, behaviourName).toString(), "UTF-8");
                                url = url.replace(':' + (String) ((HashMap) params.get(key)).get("key"), dataValue);
                                break;
                            }
                        }
                    }
                }
                ConnectionEstablishment connection = new ConnectionEstablishment();
                AsyncTask<Object, Void, Callback> execute = connection.execute(url, headers, behaviour.get("method").toString(), body, new Callback<Map<String, Object>>() {

                    @Override
                    public void callback(Map<String, Object> response, Exception exception) {

                        Map<String, Object> headers = new HashMap<>();
                        Map<String, Object> body = new HashMap<>();
                        if (behaviour.get("returns") instanceof HashMap && response != null) {

                            for (String key : ((HashMap<String, Object>) behaviour.get("returns")).keySet()) {

                                Object paramValue = null;
                                String paramKey = null;
                                String paramType = ((String) ((HashMap<String, Object>) ((HashMap<String, Object>) behaviour.get("returns")).get(key))
                                        .get("type"));
                                if (isEqual(paramType, "header")) {

                                    paramValue = ((HashMap<String, Object>) response.get("headers")).get(key);
                                    paramKey = (String) ((HashMap<String, Object>) ((HashMap<String, Object>) behaviour.get("returns")).get(key)).get("key");

                                    if (paramKey == null) paramKey = key;
                                    headers.put(paramKey, paramValue);
                                }
                                if (isEqual(paramType, "body")) {

                                    paramValue = ((HashMap<String, Object>) response.get("response")).get("response");
                                    if (paramValue instanceof HashMap)
                                        paramValue = ((HashMap<String, Object>) paramValue).get(key);
                                    paramKey = key;
                                    body.put(paramKey, paramValue);
                                }
                                Object purposes = ((HashMap<String, Object>) ((HashMap<String, Object>) behaviour.get("returns")).get(key))
                                        .get("purpose");
                                if (purposes != null && paramValue != null && paramKey != null) {

                                    if (!(purposes instanceof ArrayList)) {

                                        ArrayList<Object> purposeList = new ArrayList<Object>();
                                        purposeList.add(purposes);
                                        ((HashMap<String, Object>) ((HashMap<String, Object>) behaviour.get("returns")).get(key)).put("purpose", purposeList);
                                        purposes = purposeList;
                                    }
                                    for (Object purpose : ((ArrayList<Object>) purposes)) {

                                        switch ((String) (purpose instanceof HashMap ? ((HashMap<String, String>) purpose).get("as") : purpose)) {

                                            case "parameter":
                                                Map<String, Object> param = new HashMap<String, Object>();
                                                param.put("key", key);
                                                param.put("type", paramType);
                                                parameters.put(paramKey, param);
                                                param = getDataFromSharedPreference();
                                                param.put(paramKey, parameters.get(paramKey));
                                                if (purpose instanceof HashMap && ((HashMap<String, Object>) purpose).get("unless") != null) {

                                                    ((HashMap<String, Object>) parameters.get(paramKey)).put("unless", ((HashMap<String, Object>) purpose).get("unless"));
                                                    ((HashMap<String, Object>) param.get(paramKey)).put("unless", ((HashMap<String, Object>) purpose).get("unless"));
                                                }
                                                if (purpose instanceof HashMap && ((HashMap<String, Object>) purpose).get("for") != null) {

                                                    ((HashMap<String, Object>) parameters.get(paramKey)).put("for", ((HashMap<String, Object>) purpose).get("for"));
                                                    ((HashMap<String, Object>) param.get(paramKey)).put("for", ((HashMap<String, Object>) purpose).get("for"));
                                                }
                                                for (Object p : ((ArrayList<Object>) purposes)) {

                                                    if (isEqual(p, "constant") || (p instanceof HashMap && isEqual(((HashMap<String, Object>) p).get("as"), "constant"))) {

                                                        ((HashMap<String, Object>) parameters.get(paramKey)).put("value", paramValue);
                                                        ((HashMap<String, Object>) param.get(paramKey)).put("value", paramValue);
                                                        break;
                                                    }
                                                }
                                                putDataIntoSharedPreference(param);
                                                break;
                                        }
                                    }
                                }
                            }
                            if (!headers.isEmpty()) {

                                if (body.isEmpty())
                                    body.put("data", ((HashMap<String, Object>) response.get("response")).get("response"));
                                headers.putAll(body);
                                cb.callback(headers, new BehaviorError(exception != null ? exception.getMessage() : null));
                                return;
                            }
                        }
                        cb.callback(response != null ? (HashMap<String, Object>) ((HashMap<String, Object>) response.get("response")).get("response") : null,
                                new BehaviorError(exception != null ? exception.getMessage() : null));
                    }
                });
                return null;
            }

            @Override
            public boolean equals(Object var1, Object var2) {

                return false;
            }
        };
    }

    private HttpURLConnection getConnection(String path, Map<String, Object> headers, String method, Map<String, Object> body) throws Exception {

        URL url = null;
        url = this.getURL.apply(path);
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setRequestMethod(method);
        if (headers == null) headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        if (headers != null) {

            for (String key : headers.keySet()) {

                httpCon.setRequestProperty(key, headers.get(key).toString());
            }
        }
        if (body != null) {

            httpCon.setDoOutput(true);
            OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
            JSONObject json = new JSONObject(body);
            out.write(json.toString());
            out.close();
        }
        return httpCon;
    }

    private Map<String, Object> jsonToMap(String jsonStr) throws JSONException {

        JSONObject json = new JSONObject(jsonStr);
        Map<String, Object> retMap = new HashMap<String, Object>();
        if (json != JSONObject.NULL) {

            retMap = toMap(json);
        }
        return retMap;
    }

    private Map<String, Object> toMap(JSONObject object) throws JSONException {

        Map<String, Object> map = new HashMap<String, Object>();
        Iterator<String> keysItr = object.keys();
        while (keysItr.hasNext()) {

            String key = keysItr.next();
            Object value = object.get(key);
            if (value instanceof JSONArray) {

                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {

                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    private List<Object> toList(JSONArray array) throws JSONException {

        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < array.length(); i++) {

            Object value = array.get(i);
            if (value instanceof JSONArray) {

                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {

                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }

    private void putDataIntoSharedPreference(Map<String, Object> data) {

        SharedPreferences prefs = context.getSharedPreferences("Behaviours_Pref", Context.MODE_PRIVATE);
        prefs.edit().putString("Behaviors", new Gson().toJson(data)).apply();
    }

    private HashMap<String, Object> getDataFromSharedPreference() {

        SharedPreferences prefs = context.getSharedPreferences("Behaviours_Pref", Context.MODE_PRIVATE);
        String strData = prefs.getString("Behaviors", null);
        HashMap<String, Object> data = new HashMap<>();
        if (strData != null) {

            Gson gson = new Gson();
            Type hashMapType = new TypeToken<HashMap<String, Object>>() {
            }.getType();
            data = gson.fromJson(strData, hashMapType);
        }
        return data;
    }

    private boolean isEqual(Object o1, Object o2) {

        return o1 != null && o1.equals(o2);
    }

    private class ConnectionEstablishment extends AsyncTask<Object, Void, Callback> {

        HashMap<String, Object> response = null;
        Exception exception;

        {

            exception = null;
        }

        @Override
        protected Callback doInBackground(Object... params) {

            HttpURLConnection httpCon = null;
            Callback cb = (Callback) params[4];
            try {

                httpCon = getConnection((String) params[0], (Map<String, Object>) params[1], (String) params[2], (Map<String, Object>) params[3]);
            } catch (Exception ex) {

                exception = ex;
                ex.printStackTrace();
                return cb;
            }
            try {

                InputStream in = httpCon.getResponseCode() == HttpURLConnection.HTTP_OK ? httpCon.getInputStream() : httpCon.getErrorStream();
                StringBuilder sb = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String read;
                while ((read = br.readLine()) != null) {

                    sb.append(read);
                }
                br.close();
                response = new HashMap<String, Object>();
                response.put("response", jsonToMap(sb.toString()));
                HashMap<String, Object> headers = new HashMap<String, Object>();
                for (String key : httpCon.getHeaderFields().keySet()) {

                    headers.put(key, httpCon.getHeaderField(key));
                }
                response.put("headers", headers);
                if (httpCon.getResponseCode() != HttpURLConnection.HTTP_OK)
                    exception = new Exception(httpCon.getResponseMessage());
            } catch (Exception ex) {

                exception = ex;
                ex.printStackTrace();
            } finally {

                if (httpCon != null) httpCon.disconnect();
            }
            return cb;
        }

        @Override
        protected void onPostExecute(Callback cb) {

            super.onPostExecute(cb);
            cb.callback(response, exception);
        }
    }
}
