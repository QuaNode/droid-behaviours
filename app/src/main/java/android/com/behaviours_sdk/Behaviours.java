package android.com.behaviours_sdk;

import android.os.AsyncTask;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.roboguice.shaded.goole.common.annotations.GwtCompatible;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


interface BehaviourCallback<T> {

    void callback(T t, Error e);
}

/**
 * Created by Mohammed on 12/15/2015.
 */

@GwtCompatible
 interface Function<F,L> {
    @Nullable
    void apply(@Nullable F var1, @Nullable L var2) throws IOException;

    boolean equals(@Nullable Object var1,@Nullable Object var2);
}
@GwtCompatible
interface GETURLFunction {
    @Nullable
    URL apply(@Nullable String path) throws IOException;

    boolean equals(@Nullable Object var1);
}
public class Behaviours {

    Map<String, Object> behavioursJSON = new HashMap<String, Object>();

    GETURLFunction getURL = null;

    protected  Behaviours(GETURLFunction getURL){
        this.getURL = getURL;
    }

    protected  Behaviours(final String baseUrl){

        this.getURL = new GETURLFunction() {
            @Nullable
            @Override
            public URL apply(@Nullable String path) throws MalformedURLException {
                return new URL(baseUrl + path);
            }

            @Override
            public boolean equals(@Nullable Object var1) {
                return false;
            }
        };
    }

    private void initiateBehaviour() throws IOException {

        sendRequest("/behaviours", null, "GET", null, new BehaviourCallback<Object>() {
            @Override
            public void callback(Object o, Error e) {

                if(e != null){
                    throw (e);
                }else {
                    behavioursJSON = (Map<String, Object>) o;
                }
            }
        });
    }

    private Function<Map<String, Object>, BehaviourCallback<Object>> getBehaviour(String behaviourName) throws Exception {

        final Map<String, Object> behaviour = (Map<String, Object>) behavioursJSON.get(behaviourName);
        if (behaviour == null) {

            throw new Exception("Invalid behaviour name");
        }
        return new Function<Map<String,Object>,BehaviourCallback<Object>>() {

            @Nullable
            @Override
            public void apply(@Nullable Map<String, Object> data, @Nullable BehaviourCallback<Object> callback) throws IOException {

                Map<String,Object> headers = new TreeMap<>();
                Map<String,Object> body = new TreeMap<>();
                String url = (String) behaviour.get("path");
                for (String key : data.keySet()){

                    switch ((String)((Map<String, Object>)((Map<String, Object>)behaviour.get("parameters")).get(key)).get("type")) {

                        case "header":{
                               headers.put(key, data.get(key));
                               break;
                        }
                        case "body":{
                            String [] paths = behaviour.get(key).toString().split(".");
                            Map<String, Object> nestedData = body;
                            String lastPath = null;
                            for(String path : paths){

                                if(lastPath!= null) {

                                    nestedData = (Map<String, Object>) nestedData.get(lastPath);
                                }
                                if (nestedData.get(path) == null) {

                                    nestedData.put(path, new TreeMap<String,Object>());
                                }
                                lastPath = path;
                            }
                            if (lastPath!= null) nestedData.put(lastPath, data.get(key));
                            break;
                        }
                        case "query":{
                            if (url.indexOf('?') == -1) {

                                   url += '?';
                            }
                            String behaviourKey =  URLEncoder.encode(behaviour.get(key).toString(), "UTF-8");
                            String dataValue =  URLEncoder.encode(data.get(key).toString(), "UTF-8");
                            url += '&' + behaviourKey + '=' + dataValue;
                            break;
                       }
                        case "path": {

                            String dataValue =  URLEncoder.encode(data.get(key).toString(), "UTF-8");
                            url.replace(':' + behaviour.get(key).toString(), dataValue);
                            break;
                        }
                    }
                    sendRequest(url,headers,behaviour.get("method").toString(),body,callback);

                }
            }

            @Override
            public boolean equals(@Nullable Object var1, @Nullable Object var2) {
                return false;
            }
        };
    }

    protected void sendRequest(String path,@Nullable Map<String, Object> headers,
                               String method,@Nullable Map<String, Object> body, BehaviourCallback<Object> callback) throws IOException {

        URL url = this.getURL.apply(path);
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setRequestMethod(method);
        if(body!= null){
            httpCon.setDoOutput(true);
            OutputStreamWriter out = new OutputStreamWriter(
                    httpCon.getOutputStream());
            JSONObject json = new JSONObject(body);

            out.write(json.toString());
            out.close();
        }
        if(headers!= null){
            for(String key : headers.keySet()){
                httpCon.setRequestProperty(key,  headers.get(key).toString());
            }
        }

        ConnectionEstablishment connection = new ConnectionEstablishment();
        connection.execute(httpCon , callback);


    }
    private class ConnectionEstablishment extends AsyncTask<Object, Void, String> {
        @Override
        protected String doInBackground(Object... params) {

            HttpURLConnection httpCon = (HttpURLConnection) params[0];
            Function callback = (Function) params[1];
            try {
                InputStream in = httpCon.getInputStream();
                StringBuilder sb = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String read;

                while((read=br.readLine()) != null) {
                    sb.append(read);
                }

                br.close();

                callback.apply(sb.toString(), null);

            } catch (Exception ex){
                try {
                    callback.apply(null, ex);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            return null;
        }
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




}

