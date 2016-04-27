package com.finxl.finxlsample.cache;

import android.content.Context;

import java.io.File;

/**
 * Created by Mahesh Chauhan on 4/27/2016.
 * This class encapsulates the logic of caching a single file on the application
 * cache dir.
 */
public class ImageFileCache {
    private File mFile;

    public ImageFileCache() {

    }

    public ImageFileCache(Context context) {
        mFile = context.getCacheDir();
        if(!mFile.exists()) {
            mFile.mkdirs();
        }
    }

    public File getFile(String url) {
        String fileName = String.valueOf(url.hashCode());
        File file = new File(mFile, fileName);
        return file;
    }

    public void clear() {
        File[] files = mFile.listFiles();
        for(File f : files) {
            f.delete();
        }
    }
}
