package com.quanode.behaviours;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JSONMap {

    public static List<Object> toList(JSONArray array) throws JSONException {

        List<Object> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {

            Object value = array.get(i);
            if (value != JSONObject.NULL) {

                if (value instanceof JSONArray) {

                    value = toList((JSONArray) value);
                } else if (value instanceof JSONObject) {

                    value = toMap((JSONObject) value);
                }
                list.add(value);
            }
        }
        return list;
    }

    public static Map<String, Object> toMap(JSONObject object) throws JSONException {

        Map<String, Object> map = new HashMap<>();
        Iterator<String> keysItr = object.keys();
        while (keysItr.hasNext()) {

            String key = keysItr.next();
            if (!object.isNull(key)) {

                Object value = object.get(key);
                if (value instanceof JSONArray) {

                    value = toList((JSONArray) value);
                } else if (value instanceof JSONObject) {

                    value = toMap((JSONObject) value);
                }
                map.put(key, value);
            }
        }
        return map;
    }

    public static Map<String, Object> jsonToMap(String jsonStr) throws JSONException {

        JSONObject json = new JSONObject(jsonStr);
        Map<String, Object> retMap = new HashMap<>();
        if (json != JSONObject.NULL) {

            retMap = toMap(json);
        }
        return retMap;
    }
}
