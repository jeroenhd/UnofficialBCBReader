package nl.jeroenhd.app.bcbreader.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * API URL container
 */
public class API {
    /**
     * The default update hour for the app
     */
    public static final int DEFAULT_UPDATE_HOUR = 13;
    /**
     * The default latest chapter number for the app
     * This will change very quickly though...
     */
    public static final float DEFAULT_LATEST_CHAPTER = 95;
    /**
     * The default latest page number for the app
     * This will change very quickly though...
     */
    public static final int DEFAULT_LATEST_PAGE = 14;
    public static final String DEFAULT_UPDATE_DAYS = "1,3,5";
    /**
     * The base URL of the main website
     */
    private static final String BaseURL = "https://www.bittersweetcandybowl.com/";
    /**
     * The URL to the chapter database
     */
    public static final String ChaptersDB = BaseURL + "app/json/db_main-1.2";
    /**
     * The URL to check for new chapters/pages
     */
    public static final String CheckURI = BaseURL + "app/json/check";
    /**
     * The URL of the BCB CDN
     */
    private static final String CDNUrl = "https://blasto.enterprises/";

    /**
     * Determine whether the files for a chapter end in JPG or are not
     *
     * @param chapter The chapter number to check
     * @return True if the chapter is a JPG chapter, false otherwise
     */
    public static boolean isJpegChapter(double chapter, String quality) {
        // Exceptions to the rule below
        if (chapter == 16.1 || chapter == 17.1 || chapter == 22.1 || chapter == 26.1 || chapter == 35.0 || chapter == 35.1 || chapter == 38.1 || chapter == 102)
            return true;

        // Well this is fun
        if (quality.equals("@m")) {
            // Some of these chapters (like 94.1) are in PNG format even though they're mobile
            // Others can be categorized by range
            return chapter != 94.1 && (chapter == 35 || chapter == 50 || (chapter > 60 && chapter < 89) || chapter > 90);

        }

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

    /**
     * Get the quality suffix
     *
     * @param context The application context
     * @return Depending on the user's preferences, either "@m" for mobile, "@2x" for retina or "" for desktop quality files
     */
    @NonNull
    public static String getQualitySuffix(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences == null) {
            Log.d(App.TAG, "getQualitySuffix: Failed to obtain shared preferences, returning default");
            return "@m";
        } else {
            String p = sharedPreferences.getString("reading_quality", "-1");
            int quality = Integer.parseInt(p);

            switch (quality) {
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
     * Find the base URL for all resources
     *
     * @param context The context to check preferences with
     * @return A string containing the base URL (protocol + hostname + '/') of the resource server
     */
    private static String getResourceBaseURL(Context context) {
        if (UseCDN(context)) {
            return CDNUrl;
        } else {
            return BaseURL;
        }
    }

    /**
     * Format a URL for a page
     * This method is deprecated but kept because of version control reasons
     * Please use FormatPageUrl(chapter, page, quality) instead
     *
     * @param chapter The chapter number
     * @param page    The page number
     * @return The URL to the page
     */
    @Deprecated
    public static String FormatPageUrl(Double chapter, Double page) {
        return FormatPageUrl(chapter, page, "@m");
    }

    /**
     * Format a URL for a page
     *
     * @param chapter The chapter number
     * @param page    The page number
     * @param quality The quality of the page
     * @return The URL to the page
     * @deprecated Use FormatPageUrl(Context, Double, double, String) instead
     */
    @Deprecated
    public static String FormatPageUrl(Double chapter, double page, String quality) {
        String ext = isJpegChapter(chapter, quality) ? ".jpg" : ".png";

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
        return CDNUrl + "comics/" + FormatChapterNumber(chapter) + "/" + "" + ((long) page) + quality + ext;
    }

    /**
     * Format a URL for a page
     *
     * @param context The context for the application
     * @param chapter The chapter number
     * @param page    The page number
     * @param quality The quality of the page
     * @return The URL to the page
     */
    public static String FormatPageUrl(Context context, Double chapter, double page, String quality) {
        String ext = isJpegChapter(chapter, quality) ? ".jpg" : ".png";
        return getResourceBaseURL(context) + "comics/" + FormatChapterNumber(chapter) + "/" + "" + ((long) page) + quality + ext;
    }

    /**
     * Format a short (bcb.cat) page URL
     *
     * @param chapterNumber The chapter number
     * @param pageNumber    The page number
     * @return The short link URL to the page
     */
    public static String FormatPageLink(Double chapterNumber, long pageNumber) {
        return String.format(Locale.US, "https://bcb.cat/c%1$s/p%2$s/", FormatChapterNumber(chapterNumber), pageNumber);
    }

    @Deprecated
    public static String FormatLqThumbURL(double chapter, double page) {
        return CDNUrl + "app/comics/lqthumb/" + FormatChapterNumber(chapter) + "-" + (long) page + ".jpg";
    }

    /**
     * Format the URL to a low quality thumb preview of a page
     *
     * @param context The application context
     * @param chapter The chapter number for the page
     * @param page    The page number
     * @return A full URL to the image file containing a low quality page preview
     */
    public static String FormatLqThumbURL(Context context, double chapter, double page) {
        return getResourceBaseURL(context) + "app/comics/lqthumb/" + FormatChapterNumber(chapter) + "-" + (long) page + ".jpg";
    }

    /**
     * Turn a chapter number into a string
     *
     * @param chapter The chapter numer to format
     * @return The chapter number, either in the format ## or ##.#
     */
    public static String FormatChapterNumber(double chapter) {
        String out;

        // Check for decimals
        if (chapter == (long) chapter) {
            out = String.valueOf((long) chapter);
        } else {
            out = String.valueOf(chapter);
        }

        return out;
    }

    /**
     * Format a URL for a thumb in the chapter list
     *
     * @param chapter The chapter number to decode for
     * @return The full URL to the chapter thumb on the CDN server
     * @deprecated Use FormatChapterThumbURL(Context, Double) instead
     */
    @Deprecated
    public static String FormatChapterThumbURL(Double chapter) {
        /***
         * CDN returns 404 page instead of empty image for unknown chapters
         * This is the behaviour the app is designed for for now. This might change though,
         * so the code remains here for now.
         */
        final boolean useCDN = true;
        if (useCDN) {
            return CDNUrl + "app/comics/icon/" + FormatChapterNumber(chapter) + ".png";
        } else {
            return BaseURL + "app/comics/icon/" + FormatChapterNumber(chapter) + ".png";
        }
    }

    /**
     * Format a URL for a thumb in the chapter list
     *
     * @param chapter The chapter number to decode for
     * @return The full URL to the chapter thumb on the CDN server
     */
    public static String FormatChapterThumbURL(Context context, Double chapter) {
        //CDN returns 404 page instead of empty image for unknown chapters!!
        if (UseCDN(context)) {
            return CDNUrl + "app/comics/icon/" + FormatChapterNumber(chapter) + ".png";
        } else {
            return BaseURL + "app/comics/icon/" + FormatChapterNumber(chapter) + ".png";
        }
    }

    /**
     * Check if we need to use the CDN
     *
     * @param context Used to read the settings
     * @return True if we need to download from the CDN
     */
    private static boolean UseCDN(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean("reading_use_cdn", true);
    }

}
