package nl.jeroenhd.app.bcbreader.data.check;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.sql.language.Select;

import nl.jeroenhd.app.bcbreader.data.API;
import nl.jeroenhd.app.bcbreader.data.Chapter;
import nl.jeroenhd.app.bcbreader.data.Chapter_Table;

/**
 * A caching class which stores the latest updates in the preferences
 */

public class DataPreferences {
    /**
     * This preference key stores the update time
     */
    private static final String PREF_UPDATE_TIME = "hidden_update_time";
    /**
     * This preference key stores the update days
     */
    private static final String PREF_UPDATE_DAYS = "hidden_update_days";
    /**
     * This preference key stores the latest chapter number
     */
    private static final String PREF_LATEST_CHAPTER = "hidden_latest_chapter";
    /**
     * This preference key stores the latest page number
     */
    private static final String PREF_LATEST_PAGE = "hidden_latest_page";

    /**
     * Save a Check object to the cache
     *
     * @param context The context for the preferences
     * @param check   The check object to save
     */
    public static void SaveCheck(Context context, Check check) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();

        editor.putInt(PREF_UPDATE_TIME, Integer.parseInt(check.getUpdateTimes().getUpdateHour()));
        editor.putString(PREF_UPDATE_DAYS, check.getUpdateTimes().getUpdateDays());

        editor.putFloat(PREF_LATEST_CHAPTER, check.getAddress().getLatestChapter().floatValue());
        editor.putInt(PREF_LATEST_PAGE, check.getAddress().getLatestPage());

        editor.apply();
    }

    /**
     * Get the cached update hour
     *
     * @param context The context for the preferences
     * @return The hour of the day the comic updates in UTC time
     */
    public static int getUpdateHour(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(PREF_UPDATE_TIME, API.DEFAULT_UPDATE_HOUR);
    }

    /**
     * Get the day-of-week numbers of the update days
     *
     * @param context The context for the preferences
     * @return The days of the week the app updates
     */
    public static int[] getUpdateDays(Context context) {
        String daysString = PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_UPDATE_DAYS, API.DEFAULT_UPDATE_DAYS);
        String[] dayStrings = daysString.split(",");
        int dayInts[] = new int[dayStrings.length];

        for (int i = 0; i < dayStrings.length; i++) {
            dayInts[i] = Integer.parseInt(dayStrings[i]);
        }

        return dayInts;
    }

    /**
     * Get the latest chapter number from the cache
     *
     * @param context The context for the preferences
     * @return The latest chapter number
     */
    public static double getLatestChapterNumber(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getFloat(PREF_LATEST_CHAPTER, API.DEFAULT_LATEST_CHAPTER);
    }

    /**
     * Get the latest page number from the cache
     *
     * @param context The context for the preferences
     * @return The latest page number
     */
    public static int getLatestPage(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(PREF_LATEST_PAGE, API.DEFAULT_LATEST_PAGE);
    }

    /**
     * Get the latest chapter object based on the info in the cache
     *
     * @param context The context for the preferences
     * @return The Chapter object for the latest chapter
     */
    @Nullable
    public static Chapter getLatestChapter(Context context) {
        double chapterNumber = getLatestChapterNumber(context);
        return new Select().from(Chapter.class).where(Chapter_Table.number.eq(chapterNumber)).querySingle();
    }
}
