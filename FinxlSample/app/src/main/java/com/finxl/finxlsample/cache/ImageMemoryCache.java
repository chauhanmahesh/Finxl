package com.finxl.finxlsample.cache;

import android.graphics.Bitmap;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Mahesh Chauhan on 4/27/2016.
 */
public class ImageMemoryCache {

    // Let;s have a LRU cache.
    private Map<String, Bitmap> mCache = Collections.synchronizedMap(new LinkedHashMap<String, Bitmap>(10, 1.5f, true));
    private long mSize = 0;
    private long mLimit = 1000000;

    public ImageMemoryCache() {
        setCacheLimit(Runtime.getRuntime().maxMemory()/4);
    }

    public void setCacheLimit(long limit) {
        mLimit = limit;
    }

    public Bitmap get(String id) {
        try {
            if(!mCache.containsKey(id)) {
                return null;
            }
            return mCache.get(id);
        } catch (Exception e) {
        }
        return null;
    }

    public void put(String id, Bitmap image) {
        try {
            if(mCache.containsKey(id)) {
                mSize = getSizeInBytes(mCache.get(id));
            }
            mCache.put(id, image);
            mSize = mSize + getSizeInBytes(image);
            checkSize();
        } catch(Exception e) {

        }
    }

    private void checkSize() {
        if(mSize > mLimit) {
            Iterator<Map.Entry<String, Bitmap>> iter = mCache.entrySet().iterator();
            while(iter.hasNext()) {
                Map.Entry<String, Bitmap> entry = iter.next();
                mSize = mSize - getSizeInBytes(entry.getValue());
                iter.remove();
                if(mSize <= mLimit) {
                    break;
                }
            }
        }
    }

    public void clear() {
        try {
            mCache.clear();
            mSize = 0;
        } catch (Exception e) {

        }
    }

    private long getSizeInBytes(Bitmap bitmap) {
        if(bitmap == null) return 0;
        return bitmap.getRowBytes() * bitmap.getHeight();
    }
}
