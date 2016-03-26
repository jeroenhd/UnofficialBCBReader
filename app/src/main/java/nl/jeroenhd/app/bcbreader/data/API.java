package nl.jeroenhd.app.bcbreader.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * API URL container
 */
public class API {
    public static final String BaseURL = "https://www.bittersweetcandybowl.com/";
    public static final String ChaptersDB = BaseURL + "app/json/db_main-1.2";
    public static final String CDNUrl = "https://blasto.enterprises/";

    /**
     * Prepare request headers for special Volley requests
     * @return A Map<String, String> containing all special headers
     */
    public static Map<String, String> RequestHeaders()
    {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent","Mozilla/5.0 (Unofficial BCB Android App; " + App.Version() + "; JeroenHD) Volley/" + App.VolleyVersion());

        return headers;
    }

    public static String getQualitySuffix(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences == null)
        {
            Log.d("getQualitySuffix", "Failed to obtain shared preferences, returning default");
            return "@m";
        } else {
            String p = sharedPreferences.getString("reading_quality","-1");
            int quality = Integer.parseInt(p);

            switch (quality)
            {
                case -1:
                    return "@m";
                case 0:
                    return "";
                case 1:
                    return "@2x";
                default:
                    return "@m";
            }
        }
    }

    /**
     * Format a URL for a page
     * This method is deprecated but kept because of version control reasons
     * Please use FormatPageUrl(chapter, page, quality) instead
     * @param chapter The chapter number
     * @param page The page number
     * @return The URL to the page
     */
    @Deprecated
    public static String FormatPageUrl(Double chapter, Double page)
    {
        return FormatPageUrl(chapter, page, "@m");
    }

    /**
     * Format a URL for a page
     * @param chapter The chapter number
     * @param page The page number
     * @param quality The quality of the page
     * @return The URL to the page
     */
    public static String FormatPageUrl(Double chapter, Double page, String quality){
        return CDNUrl + "comic/" + formatChapterNumber(chapter) + "/" + page.toString() + ".png";
    }

    private static String formatChapterNumber(double chapter)
    {
        String out;
        if (chapter == (long)chapter)
        {
            out =""+((long)chapter);
        } else {
            out = chapter+"";
        }

        return out;
    }

    /**
     * Format a URL for a thumb in the chapter list
     * @param chapter The chapter number to decode for
     * @return The full URL to the chapter thumb on the CDN server
     */
    public static String FormatChapterThumbURL(Double chapter)
    {
        /***
         * CDN returns 404 page instead of empty image for unknown chapters
         */
        final boolean useCDN = true;
        if (useCDN)
        {
            return CDNUrl + "app/comics/icon/" + formatChapterNumber(chapter) + ".png";
        } else {
            return BaseURL + "app/comics/icon/" + formatChapterNumber(chapter) + ".png";
        }
    }
}
