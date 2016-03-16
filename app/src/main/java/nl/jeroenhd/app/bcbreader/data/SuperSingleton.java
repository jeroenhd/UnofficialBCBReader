package nl.jeroenhd.app.bcbreader.data;

import android.content.Context;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
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
    Network volleyNetwork;

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
        volleyCache = new DiskBasedCache(new File(mContext.getCacheDir(), "volley"), 1024 * 1024 * 128);
        volleyNetwork = new BasicNetwork(new HurlStack());

        volleyRequestQueue = new RequestQueue(volleyCache, volleyNetwork);
        volleyRequestQueue.start();
    }

    public static SuperSingleton getInstance(Context context)
    {
        if (instance != null)
            return instance;

        return (instance = new SuperSingleton(context));
    }
}
