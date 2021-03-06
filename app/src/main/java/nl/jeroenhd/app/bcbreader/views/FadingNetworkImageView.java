package nl.jeroenhd.app.bcbreader.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.util.AttributeSet;

/**
 * An ImageView that fades between loading and displaying
 * Based on https://gist.github.com/benvd/5683818
 */
public class FadingNetworkImageView extends CallbackNetworkImageView {

    public FadingNetworkImageView(Context context) {
        super(context);
    }

    public FadingNetworkImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FadingNetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        int colour;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            colour = getResources().getColor(android.R.color.transparent, getContext().getTheme());
        } else {
            //noinspection deprecation
            colour = getResources().getColor(android.R.color.transparent);
        }

        Drawable from = getDrawable();
        if (from == null)
            from = new ColorDrawable(colour);

        fadeInBitmap(from, bm);
    }

    private void fadeInBitmap(Drawable from, Bitmap bm)
    {

        TransitionDrawable td = new TransitionDrawable(new Drawable[]{
                from,
                new BitmapDrawable(getContext().getResources(), bm)
        });
        td.setCrossFadeEnabled(true);
        setImageDrawable(td);
        int fadeInTime = 250;
        td.startTransition(fadeInTime);
    }
}
