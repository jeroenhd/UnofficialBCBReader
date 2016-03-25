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
        /*PagesToCache = pages;
        ThumbsToCache = thumbs;
        LqThumbsToCache = lqthumbs;*/
        /**
         * maxSize int:
         * for caches that do not override sizeOf(K, V), this is the maximum number of entries in the cache. For all other caches, this is the maximum sum of the sizes of the entries in this cache.
         *
         * In other words, this is the max size a single item in the cache can be
         * Let's take about 3x the average page size as a maximum
         */
        super(BytesPerPage * 3);
    }

    public LruBitmapCache(int maxSize) {
        super(maxSize);
    }

    public LruBitmapCache(Context ctx) {
        this(BytesPerPage * 3);
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
}