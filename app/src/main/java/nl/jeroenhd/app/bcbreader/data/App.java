package nl.jeroenhd.app.bcbreader.data;

import nl.jeroenhd.app.bcbreader.BuildConfig;

/**
 * Keep app data together
 */
public class App {
    /**
     * The global log tag for the app
     */
    public static final String TAG = "BCBReader";

    /**
     * The full version name for this release
     *
     * @return The version name
     */
    public static String Version() {
        return BuildConfig.VERSION_NAME;
    }

    /**
     * Get the version of the Volley library
     * @return The version of the Volley library
     */
    static String VolleyVersion() {
        return "1.0.0";
    }
}
