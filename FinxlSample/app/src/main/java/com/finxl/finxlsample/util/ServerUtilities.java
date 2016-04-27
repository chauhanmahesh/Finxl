package com.finxl.finxlsample.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by Mahesh Chauhan on 4/27/2016.
 */
public final class ServerUtilities {

    private static final String TAG = ServerUtilities.class.getSimpleName();

    public static ServerUtilities instance = null;

    protected ServerUtilities() {
    }

    protected ServerUtilities(Context context) {
        Log.d(TAG, "Into ServerUtilities constructor");
    }

    public static synchronized ServerUtilities getInstance(Context context) {
        if (instance == null) {
            instance = new ServerUtilities(context);
        }
        return instance;
    }

    private String streamToString(InputStream is) throws IOException {
        String str = "";
        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is));
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
            } finally {
                is.close();
            }
            str = sb.toString();
        }
        return str;
    }

    public synchronized String sendGetRequest(final String uri){
        HttpURLConnection urlConnection = null;
        try {
            /* forming th java.net.URL object */
            URL url = new URL(uri);
            urlConnection = (HttpURLConnection) url.openConnection();
            /* optional request header */
            urlConnection.setRequestProperty("Content-Type", "application/json");
            /* optional request header */
            urlConnection.setRequestProperty("Accept", "application/json");
            /* for Get request */
            urlConnection.setRequestMethod("GET");
            int statusCode = urlConnection.getResponseCode();
            /* 200 represents HTTP OK */
            if (statusCode ==  200) {
                return streamToString(urlConnection.getInputStream());
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Checks and returns whether there is an Internet connectivity or not. This
     * would be useful to check the network connectivity before making a network
     * call.
     * 
     * @param context
     * @return "True" -> is Connected , "False" -> if not.
     */
    public synchronized static boolean isNetworkAvailable(Context context) {
        boolean isConnected = false;
        final ConnectivityManager connectivityService = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityService != null) {
            final NetworkInfo networkInfo = connectivityService
                    .getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                isConnected = true;
            }
        }
        return isConnected;
    }
}
