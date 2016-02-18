package android.com.behaviours_sdk;

import android.com.behaviours_sdk.API.BehavioursEndPoints;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.roboguice.shaded.goole.common.annotations.GwtCompatible;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

//@FunctionalInterface
interface TwoParameterFunction<X, Y> {

    Void apply(X x, Y y);
}

interface BehaviourCallback<T> {

    void callback(T t, Error e);
}

/**
 * Created by Mohammed on 12/15/2015.
 */

@GwtCompatible
interface Function<F,L> {
    @Nullable
    void apply(@Nullable F var1, @Nullable L var2);

    boolean equals(@Nullable Object var1,@Nullable Object var2);
}
public class Behaviours {

    Map<String, Object> behavioursJSON = new HashMap<String, Object>();

    protected Behaviours(RestAdapter restAdapter) {

        initiateBehaviour(restAdapter);
    }


    private void initiateBehaviour(RestAdapter restAdapter){

        BehavioursEndPoints endPoints = restAdapter.create(BehavioursEndPoints.class);
        endPoints.getBehaviours(new Callback<Response>() {
            @Override
            public void success(Response response, Response ignore) {
                Log.d("File", "Success");
                String jsonString = new String(((TypedByteArray) response.getBody()).getBytes());
                try {


                    behavioursJSON = jsonToMap(jsonString);

                } catch (JSONException e) {
                }
            }

            @Override
            public void failure(RetrofitError error) {

                Log.d("File", "Fail");
                try {
                    behavioursJSON = jsonToMap("hey");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public Function<Map<String, Object>, BehaviourCallback<Object>> getBehaviour(String behaviourName) throws Exception {

        final Map<String, Object> behaviour = (Map<String, Object>) behavioursJSON.get(behaviourName);
        if (behaviour == null) {

            throw new Exception("Invalid behaviour name");
        }
        return new Function<Map<String,Object>,BehaviourCallback<Object>>() {

            @Nullable
            @Override
            public void apply(@Nullable Map<String, Object> data, @Nullable BehaviourCallback<Object> callback) {


                for (String key : data.keySet()){

                    switch ((String)((Map<String, Object>)((Map<String, Object>)behaviour.get("parameters")).get(key)).get("type")) {

                        case "header":{


                        }
                        case "body":{

                        }
                        case "query":{

                        }
                        case "path": {
                        }
                    }



                }
            }

            @Override
            public boolean equals(@Nullable Object var1, @Nullable Object var2) {
                return false;
            }
        };
    }


    public Map<String, Object> jsonToMap(String jsonStr) throws JSONException {
        JSONObject json = new JSONObject(jsonStr);
        Map<String, Object> retMap = new HashMap<String, Object>();

        if (json != JSONObject.NULL) {
            retMap = toMap(json);
        }
        return retMap;
    }
    public Map<String, Object> toMap(JSONObject object) throws JSONException {
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

    public List<Object> toList(JSONArray array) throws JSONException {
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