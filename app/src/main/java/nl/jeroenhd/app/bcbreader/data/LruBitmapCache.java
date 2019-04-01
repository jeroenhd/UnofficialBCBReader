package nl.jeroenhd.app.bcbreader.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;

import com.android.volley.toolbox.ImageLoader;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;

/**
 * copied from official documentation
 */
class LruBitmapCache extends LruCache<String, Bitmap>
        implements ImageLoader.ImageCache {

    private LruBitmapCache(int maxSize) {
        super(maxSize);
    }

    LruBitmapCache(Context ctx) {
        this(getCacheSize(ctx));
    }

    // Returns a cache size equal to approximately three screens worth of images.
    private static int getCacheSize(Context ctx) {
        final DisplayMetrics displayMetrics = ctx.getResources().
                getDisplayMetrics();
        final int screenWidth = displayMetrics.widthPixels;
        final int screenHeight = displayMetrics.heightPixels;
        // 4 bytes per pixel
        final int screenBytes = screenWidth * screenHeight * 4;

        return screenBytes * 3;
    }

    @Override
    protected int sizeOf(@NonNull String key, @NonNull Bitmap value) {
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