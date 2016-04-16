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
    private static final String BaseURL = "https://www.bittersweetcandybowl.com/";
    public static final String ChaptersDB = BaseURL + "app/json/db_main-1.2";
    private static final String CDNUrl = "https://blasto.enterprises/";

    /**
     * Determine whether the files for a chapter end in JPG or are not
     *
     * @param chapter The chapter number to check
     * @return True if the chapter is a JPG chapter, false otherwise
     */
    public static boolean isJpegChapter(double chapter) {
        // Exceptions to the rule below
        if (chapter == 16.1 || chapter == 17.1 || chapter == 22.1 || chapter == 26.1 || chapter == 35.0 || chapter == 35.1 || chapter == 38.1)
            return true;

        // 70-88 is JPG for some reason
        return chapter >= 70 && chapter <= 88;

    }

    /**
     * Prepare request headers for special Volley requests
     *
     * @return A Map<String, String> containing all special headers
     */
    public static Map<String, String> RequestHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Unofficial BCB Android App; " + App.Version() + "; JeroenHD) Volley/" + App.VolleyVersion());

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
    public static String FormatPageUrl(Double chapter, double page, String quality) {
        /**
         * Check if we need JPG of PNG files for the chapter pages
         * Proposed fix: use CDN, leave out extension
         * Not done because: doesn't work
         *
         * 1:31 PM <jeroenhd> Quick question: I was working on my Android app and I noticed some chapters are JPG files and some are PNG files. I thought the JPGs were only for some old chapters but then I saw that 70-88 are also JPG chapters. For now I have hardcoded which chapters are JPG and which are PNG, but I fear the app will break if a future chapter will be a JPG chapter again.
         * 1:31 PM <jeroenhd> On the old server/CDN leaving out the extension redirected the client to the correct file (so /comics/88/1 would redirect to /comics/88/1.png or /comics/88/1.jpg automatically). Is there any API somewhere that helps determining what extension the images have? I could always code it so it tries the PNG and falls back to the JPG but that requires extra HTTP
         * 1:31 PM <jeroenhd> requests/network delays/battery life etc.
         * 1:59 PM <SuitCase> jpg\png should be in the api somehow? i dunno. it’s certainly in the bcb_comics mysql
         * 1:59 PM <SuitCase> i somehow resolved this for the iphone i thought
         * 1:59 PM <SuitCase> i thought i wrote a thing for nginx that does the auto resolution where it checks if it’s jpg or png
         * 2:00 PM <jeroenhd> I use the blasto.enterprises CDN but that spits out a 404 if I leave out the extension
         * 2:00 PM <SuitCase> dont use that
         * 2:00 PM <SuitCase> use the site itself, https://www.
         **/

        String ext;
        if (isJpegChapter(chapter) || quality.equals("@m"))
            ext = ".jpg";
        else
            ext = ".png";

        String url = CDNUrl + "comics/" + formatChapterNumber(chapter) + "/" + "" + ((long) page) + quality + ext;
        Log.d("FormatPageUrl", "Formatted page URL: " + url);
        return url;
    }

    public static String FormatLqThumbURL(double chapter, double page) {
        return CDNUrl + "app/comics/lqthumb/" + formatChapterNumber(chapter) + "-" + (long) page + ".jpg";
    }

    private static String formatChapterNumber(double chapter) {
        String out;
        if (chapter == (long) chapter) {
            out = "" + ((long) chapter);
        } else {
            out = chapter + "";
        }

        return out;
    }

    /**
     * Format a URL for a thumb in the chapter list
     *
     * @param chapter The chapter number to decode for
     * @return The full URL to the chapter thumb on the CDN server
     */
    public static String FormatChapterThumbURL(Double chapter) {
        /***
         * CDN returns 404 page instead of empty image for unknown chapters
         *
         */
        final boolean useCDN = true;
        if (useCDN) {
            return CDNUrl + "app/comics/icon/" + formatChapterNumber(chapter) + ".png";
        } else {
            return BaseURL + "app/comics/icon/" + formatChapterNumber(chapter) + ".png";
        }
    }

}
