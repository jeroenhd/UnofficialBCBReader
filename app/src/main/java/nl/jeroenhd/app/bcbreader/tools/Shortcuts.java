package nl.jeroenhd.app.bcbreader.tools;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.PersistableBundle;
import android.util.Log;

import java.util.Collections;

import nl.jeroenhd.app.bcbreader.BCBReaderApplication;
import nl.jeroenhd.app.bcbreader.R;
import nl.jeroenhd.app.bcbreader.activities.ChapterReadingActivity;
import nl.jeroenhd.app.bcbreader.activities.FullscreenReaderActivity;
import nl.jeroenhd.app.bcbreader.data.App;
import nl.jeroenhd.app.bcbreader.data.Chapter;
import nl.jeroenhd.app.bcbreader.data.check.DataPreferences;

/**
 * This class manages shortcuts for Android 7+ devices.
 */

public class Shortcuts {
    private static final String SHORTCUT_LATEST_PAGE = "latestPage";

    public static void Update(Context context) {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            Log.d(App.TAG, "Call to Shortcuts.Update was ignored because this device is not running Android 7.1+");
        } else {
            ShortcutManager manager = context.getSystemService(ShortcutManager.class);

            PersistableBundle latestPageBundle = new PersistableBundle();
            latestPageBundle.putBoolean(FullscreenReaderActivity.JUST_SHOW_LATEST, true);

            Intent latestPageIntent = new Intent(context, FullscreenReaderActivity.class);
            latestPageIntent.setAction(BCBReaderApplication.ACTION_SHORTCUT);

            ShortcutInfo latestPageShortCut = new ShortcutInfo.Builder(context, SHORTCUT_LATEST_PAGE)
                    .setShortLabel(context.getString(R.string.shortcut_latest_page_short))
                    .setLongLabel(context.getString(R.string.shortcut_latest_page_long))
                    .setIcon(Icon.createWithResource(context, R.drawable.ic_last_page_white_24dp))
                    .setIntent(latestPageIntent)
                    .setExtras(latestPageBundle)
                    .build();

            manager.setDynamicShortcuts(Collections.singletonList(latestPageShortCut));
        }
    }
}
