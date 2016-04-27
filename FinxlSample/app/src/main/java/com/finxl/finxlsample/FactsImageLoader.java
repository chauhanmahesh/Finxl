package com.finxl.finxlsample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.ImageView;

import com.finxl.finxlsample.cache.ImageFileCache;
import com.finxl.finxlsample.cache.ImageMemoryCache;
import com.finxl.finxlsample.util.Constants;
import com.finxl.finxlsample.util.FinxlUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Mahesh Chauhan on 4/27/2016.
 */
public class FactsImageLoader {

    // Initialize the memory cache which will hold the images.
    private ImageMemoryCache mMemoryCache = new ImageMemoryCache();
    private ImageFileCache mFileCache;
    // Initialize the Map to hold the ImageViews and the image url with it.
    private Map<ImageView, String> mImageMap =
            Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    // Let's have a executor which gonna load the images for us.
    private ExecutorService mExecutor;
    // Let's have a handler to show the images in the ImageViews from a worker thread.
    private Handler mHandler = new Handler();

    public FactsImageLoader() {

    }

    public FactsImageLoader(Context context) {
        mFileCache = new ImageFileCache(context);
        // Initialize the Executor to run the concurrent threads to load the images for the fact.
        mExecutor = Executors.newFixedThreadPool(Constants.THREAD_POOL_SIZE);
    }

    public void loadAndDisplayImage(String imageUrl, ImageView imageView) {
        // Let's first store it into the map.
        mImageMap.put(imageView, imageUrl);

        // Now let's see whether a Bitmap is stored for that image url or not in the memoryCache
        // which we have.
        Bitmap imageBitmap = mMemoryCache.get(imageUrl);
        if(imageBitmap != null) {
            // It's already available, let's set it directly
            imageView.setImageBitmap(imageBitmap);
        } else {
            // We have to download that.
            FactImage factImage = new FactImage(imageUrl, imageView);
            // Let's send it to executor to download that.
            mExecutor.submit(new ImageLoaderRunnable(factImage));
        }
    }

    private class FactImage {
        private String imageUrl;
        private ImageView imageView;
        public FactImage(String url, ImageView image) {
            imageUrl = url;
            imageView = image;
        }
    }

    private class ImageLoaderRunnable implements Runnable {
        FactImage factImage;

        public ImageLoaderRunnable(FactImage image) {
            factImage = image;
        }

        @Override
        public void run() {
            try {
                // If already downloaded, no need to download again.
                if(isImageViewExist(factImage)) {
                    return;
                }
                // Let;s get the Bitmap.
                Bitmap bitmapImage = getFactImage(factImage.imageUrl);
                // Put the image in the memoryCache.
                mMemoryCache.put(factImage.imageUrl, bitmapImage);

                DisplayImageRunnable displayImage = new DisplayImageRunnable(bitmapImage, factImage);
                mHandler.post(displayImage);
            } catch(Exception e) {

            }
        }
    }

    private class DisplayImageRunnable implements Runnable {
        Bitmap bitmap;
        FactImage factImage;

        public DisplayImageRunnable(Bitmap bitmap, FactImage factImage) {
            this.bitmap = bitmap;
            this.factImage = factImage;
        }

        @Override
        public void run() {
            if(isImageViewExist(factImage)) {
                return;
            }
            // Else let's show the image.
            if(bitmap != null) {
                factImage.imageView.setImageBitmap(bitmap);
            } else {
                // Load default resource if any.
            }
        }
    }

    public void clearCaches() {
        mMemoryCache.clear();
        mFileCache.clear();
    }

    /**
     * Downloads the Image using HttpUrlConnection.
     * @param url
     * @return
     */
    private Bitmap getFactImage(String url) {
        File file = mFileCache.getFile(url);
        // if already exist in the file cache, let;s decode it.
        Bitmap bitmap = decodeFile(file);
        if(bitmap != null) return bitmap;

        // Lets download it.
        try {
            URL imageUrl = new URL(url);
            // Open the connection.
            HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            InputStream inputStream = conn.getInputStream();

            // Let;s have a stream to write the stream to a file
            OutputStream outStream = new FileOutputStream(file);
            FinxlUtils.copyStream(inputStream, outStream);
            outStream.close();
            conn.disconnect();

            bitmap = decodeFile(file);
            return bitmap;
        } catch(Exception e) {

        }
        return null;
    }

    private Bitmap decodeFile(File file) {
        try {
            FileInputStream stream = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(stream, null, new BitmapFactory.Options());
            stream.close();
            return bitmap;
        } catch (Exception e) {

        }
        return  null;
    }

    private boolean isImageViewExist(FactImage factImage) {
        String tag = mImageMap.get(factImage.imageView);
        if(tag == null || !tag.equals(factImage.imageUrl)) {
            return true;
        }
        return false;
    }
}
