package nl.jeroenhd.app.bcbreader.data;

import android.content.Context;
import android.util.LruCache;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import com.android.volley.toolbox.ImageLoader.ImageCache;

public class LruBitmapCache extends LruCache<String, Bitmap>
        implements ImageCache {
    protected static int PagesToCache;
    protected static int ThumbsToCache;
    protected static int LqThumbsToCache;

    // Every page is about 1.5MB in size
    protected static final int BytesPerPage = 1500000;
    // Every thumb is at most 25kb in size
    protected static final int BytesPerThumb = 25000;
    // Every lq thumb is about 7.5kb in size
    protected static final int BytesPerLqThumb = 7500;

    public LruBitmapCache(int pages, int thumbs, int lqthumbs)
    {
        super(pages * 1500000 + thumbs * 25000 + lqthumbs * 7500);
        PagesToCache = pages;
        ThumbsToCache = thumbs;
        LqThumbsToCache = lqthumbs;
    }

    public LruBitmapCache(int maxSize) {
        super(maxSize);
    }

    public LruBitmapCache(Context ctx) {
        this(getCacheSize(ctx));
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value.getRowBytes() * value.getHeight();
    }

    @Override
    public Bitmap getBitmap(String url) {
        return get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        put(url, bitmap);
    }

    // Returns a cache size equal to approximately three screens worth of images.
    public static int getCacheSize(Context ctx) {
        return BytesPerPage * PagesToCache + BytesPerThumb * ThumbsToCache + BytesPerLqThumb * LqThumbsToCache;
    }
}