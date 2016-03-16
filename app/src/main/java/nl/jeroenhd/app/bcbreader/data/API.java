package nl.jeroenhd.app.bcbreader.data;

import java.util.HashMap;
import java.util.Map;

/**
 * API URL container
 */
public class API {
    public static final String BaseURL = "https://www.bittersweetcandybowl.com/app/";
    public static final String ChaptersDB = BaseURL + "json/db_main-1.2";

    public static Map<String, String> RequestHeaders()
    {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent","Mozilla/5.0 (Unofficial BCB Android App; " + App.Version() + "; JeroenHD) Volley/" + App.VolleyVersion());

        return headers;
    }
}
