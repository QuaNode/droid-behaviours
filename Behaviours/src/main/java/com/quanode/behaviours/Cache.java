package com.quanode.behaviours;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class Cache {

    private static ContextWrapper _context_;

    public Cache(ContextWrapper context) {

        _context_ = context;
    }

    public Object getValueForParameter(Map<String, Object> parameter, Map<String, Object> data,
                                       String key, String name) throws Exception {

        if (data.get(key) != null) return data.get(key);
        if (parameter.get("value") != null) return parameter.get("value") instanceof Function ?
                (((Function<String, Map<String, Object>, String>)
                        parameter.get("value"))).call(name, data) : (String) parameter.get("value");
        else if (isEqual(parameter.get("source"), true)) {

            if (getParameter().get(key) instanceof HashMap) {

                Map<String, Object> param = (Map<String, Object>) getParameter().get(key);
                return param.get("value");
            }
        }
        return null;
    }

    public void setParameter(Map<String, Object> data) {

        SharedPreferences prefs =
                _context_.getSharedPreferences("Behaviours_Pref", Context.MODE_PRIVATE);
        prefs.edit().putString("Behaviours", new Gson().toJson(data)).apply();
    }

    public HashMap<String, Object> getParameter() {

        SharedPreferences prefs = _context_.getSharedPreferences("Behaviours_Pref", Context.MODE_PRIVATE);
        String strData = prefs.getString("Behaviours", null);
        HashMap<String, Object> data = new HashMap<>();
        if (strData != null) {

            Gson gson = new Gson();
            Type hashMapType = new TypeToken<HashMap<String, Object>>() { }.getType();
            data = gson.fromJson(strData, hashMapType);
        }
        return data;
    }

    private boolean isEqual(Object o1, Object o2) {

        return o1 != null && o1.equals(o2);
    }
}
