package com.finxl.finxlsample.util;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Sonagara Prateek on 4/27/2016.
 */
public class FinxlUtils {
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
