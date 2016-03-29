package nl.jeroenhd.app.bcbreader.data;

import android.content.Context;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;
import com.google.gson.GsonBuilder;

import java.io.File;

/**
 * A singleton class to keep track of all unique objects within the app
 */
public class SuperSingleton {
    protected static SuperSingleton instance;

    private Context mContext;

    // Volley data
    RequestQueue volleyRequestQueue;
    GsonBuilder gsonBuilder;
    Cache volleyCache;
    ImageLoader.ImageCache imageCache;
    Network volleyNetwork;
    ImageLoader imageLoader;

    public GsonBuilder getGsonBuilder() {
        return gsonBuilder;
    }

    public Cache getVolleyCache() {
        return volleyCache;
    }

    public Network getVolleyNetwork() {
        return volleyNetwork;
    }

    public RequestQueue getVolleyRequestQueue() {
        return volleyRequestQueue;
    }

    protected SuperSingleton(Context context)
    {
        mContext = context;
        InitVolley();
    }

    private void InitVolley()
    {
        gsonBuilder = new GsonBuilder();
        // 1MB general cache
        volleyCache = new DiskBasedCache(new File(mContext.getCacheDir(), "volley"), 1024 * 1024 * 1024);

        // This turns out to be the RAM CACHE
        //imageCache = new LruBitmapCache(3);
        imageCache = new LruBitmapCache(mContext);
        volleyNetwork = new BasicNetwork(new HurlStack());

        volleyRequestQueue = new RequestQueue(volleyCache, volleyNetwork);
        volleyRequestQueue.start();

        imageLoader = new ImageLoader(volleyRequestQueue, imageCache);
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    public ImageLoader.ImageCache getImageCache() {
        return imageCache;
    }

    public static SuperSingleton getInstance(Context context)
    {
        if (instance != null)
            return instance;

        return (instance = new SuperSingleton(context));
    }
}
