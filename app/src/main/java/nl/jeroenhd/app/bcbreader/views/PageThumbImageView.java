package nl.jeroenhd.app.bcbreader.views;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import androidx.core.content.ContextCompat;

import com.android.volley.toolbox.ImageLoader;

import nl.jeroenhd.app.bcbreader.data.API;
import nl.jeroenhd.app.bcbreader.data.SuperSingleton;

/**
 * An image view for page thumbs loaded from the network
 */

public class PageThumbImageView extends PageImageView {
    private final Context mContext;
    private final ImageLoader imageLoader;
    private int backgroundColorId = android.R.color.white;

    public PageThumbImageView(Context context) {
        super(context);
        mContext = context;
        imageLoader = SuperSingleton.getInstance(context).getImageLoader();
    }

    public PageThumbImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        imageLoader = SuperSingleton.getInstance(context).getImageLoader();
    }

    public PageThumbImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        imageLoader = SuperSingleton.getInstance(context).getImageLoader();
    }

    public void setBackgroundColorId(int backgroundColorId) {
        this.backgroundColorId = backgroundColorId;
    }

    @Override
    public void setPage(double chapter, double page) {
        String fullURL = API.FormatLqThumbURL(mContext, chapter, page);

        // Fix showing previous (cached) image
        Drawable currentDrawable = getDrawable();
        ColorDrawable colorDrawable = new ColorDrawable(ContextCompat.getColor(getContext(), this.backgroundColorId));
        colorDrawable.setBounds(currentDrawable.getBounds());
        this.setImageDrawable(colorDrawable);

        this.setImageUrl(fullURL, this.imageLoader);
    }
}
