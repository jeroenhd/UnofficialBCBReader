package nl.jeroenhd.app.bcbreader.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;

import com.android.volley.toolbox.ImageLoader;

import nl.jeroenhd.app.bcbreader.R;
import nl.jeroenhd.app.bcbreader.data.API;
import nl.jeroenhd.app.bcbreader.data.Page;
import nl.jeroenhd.app.bcbreader.data.SuperSingleton;

/**
 * A class to view pages
 */
public class PageImageView extends FadingNetworkImageView{
    ImageLoader imageLoader;
    boolean fullImageLoaded = false;

    public PageImageView(Context context) {
        super(context);
        imageLoader = SuperSingleton.getInstance(context).getImageLoader();
    }

    public PageImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        imageLoader = SuperSingleton.getInstance(context).getImageLoader();
    }

    public PageImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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

        String thumbURL = API.FormatLqThumbURL(chapter, page);
        String fullURL = API.FormatPageUrl(chapter, page);

        // This should be very quick
        this.setImageUrl(thumbURL, this.imageLoader);
        this.setImageUrl(fullURL, this.imageLoader);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        if (bm==null)
        {
            Log.e("setImageBitmap", "Bitmap is null!");
            return;
        }
        if (bm.getWidth() < Page.NORMAL_WIDTH)
        {

            // LqThumb
            if (!fullImageLoaded)
            {
                super.setImageBitmap(bm);
            } else {
                // Already loaded
                Log.w("setImageBitmap", "Setting LqThumb bitmap while the full images has already been set??? How about no");
            }
        } else {
            // Normal page
            if (fullImageLoaded)
            {
                Log.w("setImageBitmap", "Setting full image even though it has been downloaded before!");
            }

            super.setImageBitmap(bm);
            fullImageLoaded = true;
        }
    }
}
