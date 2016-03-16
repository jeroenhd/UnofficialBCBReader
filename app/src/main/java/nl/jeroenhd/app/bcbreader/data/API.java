package nl.jeroenhd.app.bcbreader.data;

import java.util.HashMap;
import java.util.Map;

/**
 * API URL container
 */
public class API {
    public static final String BaseURL = "https://www.bittersweetcandybowl.com/app/";
    public static final String ChaptersDB = BaseURL + "json/db_main-1.2";
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

    /**
     * Format a URL for a page
     * @param chapter The chapter number
     * @param page The page number
     * @return The URL to the page
     */
    public static String FormatPageUrl(Double chapter, Double page) {
        return CDNUrl + "comic/" + chapter.toString() + "/" + page.toString() + ".png";
    }

    /**
     * Format a URL for a thumb in the chapter list
     * @param chapter The chapter number to decode for
     * @return The full URL to the chapter thumb on the CDN server
     */
    public static String FormatChapterThumbURL(Double chapter)
    {
        return CDNUrl + "app/comics/icon/" + chapter.toString() + ".png";
    }
}
