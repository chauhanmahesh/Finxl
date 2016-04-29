package com.finxl.finxlsample.cache;

import android.graphics.Bitmap;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Mahesh Chauhan on 4/27/2016.
 *
 * Encapsulates the logic of caching the images bitmap. As Bitmap object is heavy to keep in memory, we are keeping
 * its cache in the LRU cache form. Physically the images are there in the Cache directory provided
 * to the application. However, to support fast loading of images we are keeping in memory
 * after caching those.
 */
public class ImageMemoryCache {

    // Let;s have a LRU cache.
    private Map<String, Bitmap> mCache = Collections.synchronizedMap(new LinkedHashMap<String, Bitmap>(10, 1.5f, true));
    private long mSize = 0;
    private long mLimit = 1000000;

    public ImageMemoryCache() {
        setCacheLimit(Runtime.getRuntime().maxMemory()/4);
    }

    /**
     * Sets the cache limit. Ideally, the cache limit should not be
     * more than what we currently have in total.
     *
     * @param limit
     */
    public void setCacheLimit(long limit) {
        mLimit = limit;
    }

    /**
     * Gets the Bitmap from the id. Retrieves it from the LRU cache map.
     * @param id
     * @return
     */
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

    /**
     * Put the image Bitmap in the memory cache (LRU map).
     * @param id
     * @param image
     */
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

    /**
     * Checks the Size of the cache.
     */
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

    /**
     * Clears the memory cache.
     */
    public void clear() {
        try {
            mCache.clear();
            mSize = 0;
        } catch (Exception e) {

        }
    }

    /**
     * Gets the bitmap size in Bytes.
     * @param bitmap
     * @return
     */
    private long getSizeInBytes(Bitmap bitmap) {
        if(bitmap == null) return 0;
        return bitmap.getRowBytes() * bitmap.getHeight();
    }
}
