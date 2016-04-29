package com.finxl.finxlsample.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Mahesh Chauhan on 4/27/2016.
 *
 * Utility class to provide API's required.
 */
public class FinxlUtils {

    /**
     * Copy the InputStream to the output stream by reading bytes.
     * @param in
     * @param out
     */
    public static void copyStream(InputStream in, OutputStream out) {
        final int bufferSize = 1024;
        try {
            byte[] bytes = new byte[bufferSize];
            while(true) {
                int count = in.read(bytes, 0, bufferSize);
                if(count == -1) {
                    break;
                }
                out.write(bytes, 0, count);
            }
        } catch (Exception e) {

        }
    }
}
