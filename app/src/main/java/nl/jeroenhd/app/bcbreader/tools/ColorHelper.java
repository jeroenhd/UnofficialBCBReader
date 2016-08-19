package nl.jeroenhd.app.bcbreader.tools;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;

/**
 * A class to help with coloring
 */
public class ColorHelper {
    /***
     * Code stolen from
     * http://stackoverflow.com/questions/1855884/determine-font-color-based-on-background-color
     *
     * @param backgroundColor The background color to draw text on
     * @return The color that the text should be
     */
    public static int foregroundColor(int backgroundColor) {
        if (android.R.color.transparent == backgroundColor)
            return Color.rgb(0, 0, 0);

        int d;
        int[] rgb = {Color.red(backgroundColor), Color.green(backgroundColor), Color.blue(backgroundColor)};

        double a = 1 - (0.299 * rgb[1] + 0.587 * rgb[2] + 0.114 * rgb[2]) / 255.0;

        if (a < 0.5)
            d = 0;
        else
            d = 255;

        return Color.rgb(d, d, d);
    }

    /**
     * Get a color from the correct API
     *
     * @param context The context to load the resources with
     * @param colorId The ID of the color to load
     * @return The loaded color as an integer
     */
    public static int getColor(Context context, int colorId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getColor(colorId);
        } else {
            return context.getResources().getColor(colorId);
        }
    }
}
