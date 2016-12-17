package nl.jeroenhd.app.bcbreader.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.android.volley.toolbox.NetworkImageView;

/**
 * A NetworkImageView with some callbacks.
 */
public class CallbackNetworkImageView extends NetworkImageView {
    protected ImageEventListener callback;
    protected int mErrorResId;

    public CallbackNetworkImageView(Context context) {
        super(context);
    }

    public CallbackNetworkImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CallbackNetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOnImageEventListener(ImageEventListener callback) {
        this.callback = callback;
    }

    /**
     * Sets the error image resource ID to be used for this view in the event that the image
     * requested fails to load.
     *
     * @param errorImage The image to show when an error occurs
     */
    @Override
    public void setErrorImageResId(int errorImage) {
        mErrorResId = errorImage;
        super.setErrorImageResId(errorImage);
    }

    /**
     * Sets a drawable as the content of this ImageView.
     * <p/>
     * <p class="note">This does Bitmap reading and decoding on the UI
     * thread, which can cause a latency hiccup.  If that's a concern,
     * consider using {@link #setImageDrawable(Drawable)} or
     * {@link #setImageBitmap(Bitmap)} and
     * {@link BitmapFactory} instead.</p>
     *
     * @param resId the resource identifier of the drawable
     *
     */
    @Override
    public void setImageResource(int resId) {
        if (resId == mErrorResId)
        {
            if (callback != null)
                callback.onLoadError();
        }
        super.setImageResource(resId);
    }

    /**
     * Sets a Bitmap as the content of this ImageView.
     *
     * @param bm The bitmap to set
     */
    @Override
    public void setImageBitmap(Bitmap bm) {
        if (callback!=null)
            callback.onLoadSuccess(bm);
        super.setImageBitmap(bm);
    }

    public interface ImageEventListener {
        void onLoadSuccess(Bitmap bitmap);

        @SuppressWarnings("EmptyMethod")
        void onLoadError();
    }

}
