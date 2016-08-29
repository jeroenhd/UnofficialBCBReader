package nl.jeroenhd.app.bcbreader.data;

import nl.jeroenhd.app.bcbreader.BuildConfig;

/**
 * Keep app data together
 */
class App {
    public static String Version() {
        return BuildConfig.VERSION_NAME;
    }

    public static String VolleyVersion() {
        return "1.0.0";
    }
}
