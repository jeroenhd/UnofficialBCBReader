package nl.jeroenhd.app.bcbreader.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;

import com.android.volley.toolbox.ImageLoader;

import nl.jeroenhd.app.bcbreader.R;
import nl.jeroenhd.app.bcbreader.data.API;
import nl.jeroenhd.app.bcbreader.data.SuperSingleton;

/**
 * A class to view pages
 */
public class PageImageView extends FadingNetworkImageView{
    private final ImageLoader imageLoader;
    Context mContext;

    public PageImageView(Context context) {
        super(context);
        mContext = context;
        imageLoader = SuperSingleton.getInstance(context).getImageLoader();
    }

    public PageImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        imageLoader = SuperSingleton.getInstance(context).getImageLoader();
    }

    public PageImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        imageLoader = SuperSingleton.getInstance(context).getImageLoader();
    }

    public void setPage(double chapter, double page)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.setImageDrawable(getResources().getDrawable(R.drawable.dummy_page, getContext().getTheme()));
        } else {
            //noinspection deprecation
            this.setImageDrawable(getResources().getDrawable(R.drawable.dummy_page));
        }

        String fullURL = API.FormatPageUrl(mContext, chapter, page, API.getQualitySuffix(getContext()));

        this.setImageUrl(fullURL, this.imageLoader);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        if (bm==null)
        {
            return;
        }
        super.setImageBitmap(bm);
    }

    /**
     * Overridden to prevent ImageView from being stretched/centered in landscape mode
     * Stolen from http://stackoverflow.com/questions/13992535/android-imageview-scale-smaller-image-to-width-with-flexible-height-without-crop
     *
     * @param widthMeasureSpec  No idea
     * @param heightMeasureSpec No idea
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final Drawable d = getDrawable();

        if (d != null) {
            final int width = MeasureSpec.getSize(widthMeasureSpec);
            final int height = (int) Math.ceil(width * (float) d.getIntrinsicHeight() / (float) d.getIntrinsicWidth());
            this.setMeasuredDimension(width, height);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
