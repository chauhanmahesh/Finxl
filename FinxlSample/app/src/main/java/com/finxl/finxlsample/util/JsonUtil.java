package com.finxl.finxlsample.util;

import android.content.Context;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.lang.reflect.Type;

/**
 * Created by Mahesh Chauhan on 4/27/2016.
 */
public class JsonUtil {
    private static final String TAG = JsonUtil.class.getSimpleName();

    public static Object parseJsonToType(Context context,
            JSONObject jsonObject, Type type) {
        Gson gson = new Gson();
        Object result = gson.fromJson(jsonObject.toString(), type);
        return result;
    }

    public static String convertTypeToJson(Context context, Object object,
            Type type) {
        Gson gson = new Gson();
        String result = gson.toJson(object, type);
        return result;
    }

}
