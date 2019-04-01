package nl.jeroenhd.app.bcbreader.data.check;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.raizlabs.android.dbflow.sql.language.Select;

import androidx.annotation.Nullable;
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
     * This preference key stores the last read page
     */
    private static final String PREF_LAST_READ_PAGE = "hidden_last_read_page";
    /**
     * This preference key stores the last read chapter number
     */
    private static final String PREF_LAST_READ_CHAPTER = "hidden_last_read_chapter";
    private static final String PREF_LAST_NOTIFICATION_TIME = "last_notification_time";

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

        editor.putLong(PREF_LATEST_CHAPTER, Double.doubleToLongBits(check.getAddress().getLatestChapter()));
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
        String[] dayStrings;
        if (daysString != null){
            dayStrings = daysString.split(",");
        }  else {
            dayStrings = new String[0];
        }
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
        return Double.longBitsToDouble(PreferenceManager
                .getDefaultSharedPreferences(context)
                .getLong(PREF_LATEST_CHAPTER, 1));
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
        return new Select()
                .from(Chapter.class)
                .where(Chapter_Table.number.eq(chapterNumber))
                .querySingle();
    }

    /**
     * Set the page read last by the user
     *
     * Contains fix from:
     * https://stackoverflow.com/questions/16319237/cant-put-double-sharedpreferences
     *
     * @param mContext The context of the reading fragment
     * @param chapter  The chapter number
     * @param page     The page number
     */
    public static void setLastReadPage(Context mContext, double chapter, int page) {
        SharedPreferences.Editor edit = PreferenceManager
                .getDefaultSharedPreferences(mContext)
                .edit();
        edit.putInt(PREF_LAST_READ_PAGE, page);

        edit.putLong(PREF_LAST_READ_CHAPTER, Double.doubleToLongBits(chapter));
        edit.apply();
    }

    /**
     * Get the chapter number of the chapter the user read last
     *
     * Contains fix from:
     * https://stackoverflow.com/questions/16319237/cant-put-double-sharedpreferences
     * @param context The context to use
     * @return The chapter number or 1 if no number was saved
     */
    public static Double getLastReadChapterNumber(Context context) {
        return Double.longBitsToDouble(PreferenceManager
                .getDefaultSharedPreferences(context)
                .getLong(PREF_LAST_READ_CHAPTER, 1));
    }

    /**
     * Get the page number of the page the user read last
     *
     * @param context The context to use
     * @return The page number or 1 if no number was saved
     */
    public static int getLastReadPageNumber(Context context) {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getInt(PREF_LAST_READ_PAGE, 1);
    }

    /**
     * Get the last time a notification has been shown
     *
     * @param context The context to use
     * @return A Date object containing the last notification
     */
    public static long getLastNotificationTime(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong(PREF_LAST_NOTIFICATION_TIME, 0);
    }

    /**
     * Set the last time a notification has been shown to the current time
     *
     * @param context The context to use
     */
    public static void setLastNotificationDate(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(PREF_LAST_NOTIFICATION_TIME, System.currentTimeMillis()).apply();
    }
}
