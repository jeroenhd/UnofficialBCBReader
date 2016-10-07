package nl.jeroenhd.app.bcbreader.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import java.util.Calendar;
import java.util.TimeZone;

import nl.jeroenhd.app.bcbreader.data.API;
import nl.jeroenhd.app.bcbreader.data.SuperSingleton;
import nl.jeroenhd.app.bcbreader.data.check.Check;
import nl.jeroenhd.app.bcbreader.data.check.UpdateTimes;

/**
 * Sets alarms and things
 */
public class NotificationPlanner {
    /**
     * Start the notification planner process
     * THIS variation is asynchronous
     *
     * @param context The application context, required for showing notifications
     */
    public static void StartNotifications(final Context context) {
        StringRequest stringRequest = new StringRequest(
                API.CheckURI,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Gson gson = SuperSingleton.getInstance(context).getGsonBuilder().create();
                        Check check = gson.fromJson(response, Check.class);

                        PlanNotification(context, check.getUpdateTimes());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("NotificationPlanner", "Failed to obtain check data from the network!");
                        error.printStackTrace();
                    }
                });
        SuperSingleton.getInstance(context).getVolleyRequestQueue().add(stringRequest);
    }

    public static void StartNotifications(final Context context, UpdateTimes updateTimes) {
        PlanNotification(context, updateTimes);
    }

    private static void PlanNotification(Context context, UpdateTimes updateTimes) {
        PlanNotification(context, updateTimes, false);
    }

    private static void PlanNotification(Context context, UpdateTimes updateTimes, boolean debug) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        if (debug) {
            // Debug: show a notification 1 minute in the future
            calendar.add(Calendar.MINUTE, 1);
        } else {
            //There's a time difference between EST/UTC!

            calendar.set(Calendar.HOUR, Integer.parseInt(updateTimes.getUpdateHour()));
            calendar.set(Calendar.MINUTE, 0);
        }

        Intent i = new Intent(context, UpdateBroadcastReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, UpdateBroadcastReceiver.NewChapterNotificationId, i, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
    }

    public static void StartNotificationsDebug(Context context) {
        UpdateTimes updateTimes = new UpdateTimes("N/A", "N/A", "1,2,3,4,5,6,7", Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + "");
        PlanNotification(context, updateTimes, true);
    }
}
