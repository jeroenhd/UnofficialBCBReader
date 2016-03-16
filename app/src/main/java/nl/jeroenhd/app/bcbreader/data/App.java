package nl.jeroenhd.app.bcbreader.data;

import android.content.pm.PackageInfo;

import nl.jeroenhd.app.bcbreader.BuildConfig;

/**
 * Keep app data together
 */
public class App {
    public static String Version()
    {
        return BuildConfig.VERSION_NAME;
    }

    public static String VolleyVersion()
    {
        return "1.0.19";
    }
}
