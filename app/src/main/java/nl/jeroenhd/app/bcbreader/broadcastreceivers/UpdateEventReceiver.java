package nl.jeroenhd.app.bcbreader.broadcastreceivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.util.Calendar;
import java.util.TimeZone;

import nl.jeroenhd.app.bcbreader.data.App;
import nl.jeroenhd.app.bcbreader.data.check.DataPreferences;
import nl.jeroenhd.app.bcbreader.notifications.NotificationService;

/**
 * A class to receive basic notification events in
 */

public class UpdateEventReceiver extends WakefulBroadcastReceiver {
    private static final String ACTION_START_NOTIFICATION_SERVICE = "nl.jeroenhd.app.bcbreader.broadcastreceivers.START_NOTIFICATION_SERVICE";
    private static final String ACTION_DISMISS_NOTIFICATION = "nl.jeroenhd.app.bcbreader.broadcastreceivers.DISMISS_NOTIFICATION";

    /**
     * Set the alarm (call this at the start of the app)
     *
     * @param context The context to execute the call under
     */
    public static void setupAlarm(Context context) {
        // Check if the user has enable notifications, if not, stop
        // By default notifications should be disabled
        if (PreferenceManager
                .getDefaultSharedPreferences(context)
                .getBoolean("notifications_enabled", false)) {
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmIntent = getStartPendingIntent(context);

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, getStartingTime(context), AlarmManager.INTERVAL_DAY, alarmIntent);
    }

    /**
     * Get the first trigger in milliseconds
     *
     * @param context The context to execute the call under
     * @return The time, in milliseconds, for the first trigger of the notification
     */
    public static long getStartingTime(Context context) {
        //There's a time difference between EST/UTC!
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        calendar.set(Calendar.HOUR_OF_DAY, DataPreferences.getUpdateHour(context));
        calendar.set(Calendar.MINUTE, 0);

        return calendar.getTimeInMillis();
    }

    /**
     * Create a pending intent with a specified action
     *
     * @param context The context to create the intent with
     * @param action  The action to set for htis intent
     * @return The created intent
     */
    private static PendingIntent getPendingIntent(Context context, String action) {
        Intent intent = new Intent(context, UpdateEventReceiver.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Get the pending intent to start the notification service
     *
     * @param context The context to execute the call under
     * @return The PendingIntent for use in planning the service start
     */
    static PendingIntent getStartPendingIntent(Context context) {
        return getPendingIntent(context, ACTION_START_NOTIFICATION_SERVICE);
    }

    /**
     * Get the pending intent to dismiss the notification service
     *
     * @param context The context to execute the call under
     * @return The PendingIntent for use in planning the service dismissal
     */
    static PendingIntent getDismissPendingIntent(Context context) {
        return getPendingIntent(context, ACTION_DISMISS_NOTIFICATION);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Intent service = null;

        if (ACTION_START_NOTIFICATION_SERVICE.equals(action)) {
            service = NotificationService.createStartServiceIntent(context);
        } else if (ACTION_DISMISS_NOTIFICATION.equals(action)) {
            service = NotificationService.createStopServiceIntent(context);
        } else {
            Log.d(App.TAG, "Received an invalid intent");
        }

        if (service != null) {
            startWakefulService(context, service);
        }
    }
}
