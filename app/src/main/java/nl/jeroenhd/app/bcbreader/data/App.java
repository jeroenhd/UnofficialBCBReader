package nl.jeroenhd.app.bcbreader.data;

import nl.jeroenhd.app.bcbreader.BuildConfig;

/**
 * Keep app data together
 */
public class App {
    public static final String TAG = "BCBReader";

    public static String Version() {
        return BuildConfig.VERSION_NAME;
    }

    static String VolleyVersion() {
        return "1.0.0";
    }
}
