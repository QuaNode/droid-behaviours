package com.quanode.behaviours;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class Cache {

    public Object getValueForParameter(Map<String, Object> parameter, Map<String, Object> data, String key, String name)
            throws Exception {

        if (data.get(key) != null) return data.get(key);
        if (parameter.get("value") != null) return parameter.get("value") instanceof Function ?
                (((Function<String, Map<String, Object>, String>) parameter.get("value"))).apply(name, data) :
                (String) parameter.get("value");
        else if (isEqual(parameter.get("source"), true)) {

            if (getDataFromSharedPreference().get(key) instanceof HashMap) {

                Map<String, Object> param = (Map<String, Object>) getDataFromSharedPreference().get(key);
                if (param.get("value") != null) return param.get("value");
            }
        }
        return null;
    }

    public static Application _context_;
    public HashMap<String, Object> getDataFromSharedPreference() {

        SharedPreferences prefs = _context_.getSharedPreferences("Behaviours_Pref", Context.MODE_PRIVATE);
        String strData = prefs.getString("Behaviours", null);
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
}
