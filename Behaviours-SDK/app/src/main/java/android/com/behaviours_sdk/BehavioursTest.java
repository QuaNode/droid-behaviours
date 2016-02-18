package android.com.behaviours_sdk;

import android.com.behaviours_sdk.API.BehavioursEndPoints;
import android.com.behaviours_sdk.Model.MockClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

/**
 * Created by Mohammed on 2/8/2016.
 */
public class BehavioursTest {
    String API = "https://api.stackexchange.com/";                         //BASE URL

    private void initiateBehavoir(){

        RestAdapter.Builder builder = new RestAdapter.Builder();
        builder.setClient(new MockClient());
        RestAdapter restAdapter =
                builder.setClient(new MockClient())
                        .setEndpoint(API)
                        .build();

        BehavioursEndPoints git = restAdapter.create(BehavioursEndPoints.class);

        git.getBehaviours(new Callback<Response>() {
            @Override
            public void success(Response response, Response ignore) {

                String jsonString = new String(((TypedByteArray) response.getBody()).getBytes());
                // JSON to Map
                Map<String, Object> jsonMap = new HashMap<String, Object>();
                try {
                    //JSONObject json = new JSONObject(jsonString);
                    jsonMap = jsonToMap(jsonString);
                } catch (JSONException e) {
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }

            /*
                here it will return lamdaa expersion
                check name of behavoiud exist in the json
                the lmada will get data object
                and map the object to the header and make request
             */
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


        });
    }
}
