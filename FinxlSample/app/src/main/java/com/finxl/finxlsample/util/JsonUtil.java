package com.finxl.finxlsample.util;

import android.content.Context;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.lang.reflect.Type;

/**
 * Created by Mahesh Chauhan on 4/27/2016.
 *
 * A generic parser to parse the json to the object type or vice-versa.
 * Application has to either call:
 * parseJsonToType - converts the jsonObject to the Model class.
 * convertTypeToJson - converts the model class object to the json string.
 */
public class JsonUtil {
    private static final String TAG = JsonUtil.class.getSimpleName();

    /**
     * Converts the JsonObject to the Model class object whose Type is sent as API parameters
     * @param context
     * @param jsonObject
     * @param type
     * @return
     */
    public static Object parseJsonToType(Context context,
            JSONObject jsonObject, Type type) {
        Gson gson = new Gson();
        Object result = gson.fromJson(jsonObject.toString(), type);
        return result;
    }

    /**
     * Converts the Model object into the Json string.
     * @param context
     * @param object
     * @param type
     * @return
     */
    public static String convertTypeToJson(Context context, Object object,
            Type type) {
        Gson gson = new Gson();
        String result = gson.toJson(object, type);
        return result;
    }

}
