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
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by Mahesh Chauhan on 4/27/2016.
 *
 * This takes care of actually loading the image from the server and setting it
 * to the ImageView of the ListView row.
 * It also takes care of checking the image first in the cache before actually
 * downloadin it.
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
    private Map<String, Boolean> serverRequestMap  = Collections.synchronizedMap(new HashMap<String, Boolean>());

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
            imageView.setImageResource(0);
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

    /**
     * A task which basically takes care of loading(either from cache or downloadin it) the iamge.
     */
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
                Bitmap bitmapImage = null;
                Boolean isServerRequestExists = serverRequestMap.get(factImage.imageUrl);
                isServerRequestExists = (isServerRequestExists == null ? false : isServerRequestExists);

                if(!isServerRequestExists)
                {
                    serverRequestMap.put(factImage.imageUrl, true);
                    try
                    {
                        bitmapImage = getFactImage(factImage.imageUrl);
                    }
                    catch(Exception exception)
                    {
                        serverRequestMap.put(factImage.imageUrl, false);
                        if(isImageViewExist(factImage))
                        {
                            return;
                        }
                        bitmapImage = getFactImage(factImage.imageUrl);
                        serverRequestMap.put(factImage.imageUrl, true);
                    }
                }
                else
                {
                    return;
                }

                if(bitmapImage != null)
                {
                    serverRequestMap.remove(factImage.imageUrl);
                    mMemoryCache.put(factImage.imageUrl, bitmapImage);
                }

                if(isImageViewExist(factImage))
                {
                    return;
                }

                DisplayImageRunnable bitmapDisplayer = new DisplayImageRunnable(bitmapImage, factImage);
                // Let;s show the image now. Tell the handler to do the operation as we are in a worker
                // thread.
                mHandler.post(bitmapDisplayer);
            } catch(Exception e) {

            }
        }
    }

    /**
     * Task which actually takes care of displaying the image on the ImageView.
     */
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
                factImage.imageView.setImageResource(0);
            }
        }
    }


    /**
     * Clears the various caches.
      */
    void clearCaches() {
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
            if (conn instanceof HttpsURLConnection) {
                ((HttpsURLConnection)conn).setSSLSocketFactory(trustAllHosts());
            }
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

    private SSLSocketFactory trustAllHosts() {
        X509TrustManager easyTrustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {


            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

        };
        TrustManager[] trustAllCerts = new TrustManager[] {easyTrustManager};
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            return sc.getSocketFactory();
        } catch (Exception e) {

        }
        return null;
    }

    /**
     * Decode the file to get the Image Bitmap.
     * @param file
     * @return
     */
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

    /**
     * Checks whether the image if already present in the map or not.
     * @param factImage
     * @return
     */
    private boolean isImageViewExist(FactImage factImage) {
        String tag = mImageMap.get(factImage.imageView);
        if(tag == null || !tag.equals(factImage.imageUrl)) {
            return true;
        }
        return false;
    }
}
