package nl.jeroenhd.app.bcbreader.tools;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;

/**
 * A helper class to help with app compatibility
 */

public class CompatHelper {
    /**
     * A helper function to use the modern fromHtml method on N+ devices
     *
     * @param htmlCode The HTML string to parse
     * @return The Spanned object from Html.fromHtml
     */
    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String htmlCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(htmlCode, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(htmlCode);
        }
    }

    /**
     * Get a color from the correct API
     *
     * @param context The context to load the resources with
     * @param colorId The ID of the color to load
     * @return The loaded color as an integer
     */
    @SuppressWarnings("deprecation")
    public static int getColor(Context context, int colorId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getColor(colorId);
        } else {
            return context.getResources().getColor(colorId);
        }
    }
}
