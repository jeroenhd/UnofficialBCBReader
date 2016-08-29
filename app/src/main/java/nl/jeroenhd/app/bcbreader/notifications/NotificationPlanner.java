package nl.jeroenhd.app.bcbreader.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.util.TimeZone;

import nl.jeroenhd.app.bcbreader.data.check.UpdateTimes;

/**
 * Sets alarms and things
 */
public class NotificationPlanner {
    private final Context context;
    private NotificationPlanner planner;

    private NotificationPlanner(Context context) {
        this.context = context;
    }

    public void PlanNotification(UpdateTimes updateTimes) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        //EST!
        calendar.set(Calendar.HOUR, Integer.parseInt(updateTimes.getUpdateHour()));
        calendar.set(Calendar.MINUTE, 0);

        Intent i = new Intent(context, UpdateBroadcastReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, UpdateBroadcastReceiver.NewChapterNotificationId, i, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
    }
}
