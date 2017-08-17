package nl.jeroenhd.app.bcbreader.notifications;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonRequest;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import nl.jeroenhd.app.bcbreader.R;
import nl.jeroenhd.app.bcbreader.activities.FullscreenReaderActivity;
import nl.jeroenhd.app.bcbreader.data.API;
import nl.jeroenhd.app.bcbreader.data.App;
import nl.jeroenhd.app.bcbreader.data.Chapter;
import nl.jeroenhd.app.bcbreader.data.ChapterListRequest;
import nl.jeroenhd.app.bcbreader.data.SuperSingleton;
import nl.jeroenhd.app.bcbreader.data.check.Check;
import nl.jeroenhd.app.bcbreader.data.check.DataPreferences;
import nl.jeroenhd.app.bcbreader.data.databases.ChapterDatabase;

/**
 * The intent service to show and dismiss the notifications
 */

public class NotificationService extends IntentService {
    /**
     * The (very creative) ID for the notification
     */
    public static final int NewChapterNotificationId = 0xB00B;
    /**
     * The start action for this service
     */
    public static final String ACTION_START = "nl.jeroenhd.app.bcbreader.notifications.START";
    /**
     * The stop action for this service
     */
    public static final String ACTION_STOP = "nl.jeroenhd.app.bcbreader.notifications.STOP";
    /**
     * A stupid hack to make the context of the service available for its Volley listeners.
     * This will probably break at some point in the future
     */
    private final Context leakingContext = this;
    /**
     * The amount of times the update check has failed
     */
    private int errorCount;
    /**
     * A listener to receive the result of an update check
     */
    private final Response.Listener<Check> checkListener = new Response.Listener<Check>() {
        @Override
        public void onResponse(Check response) {
            DataPreferences.SaveCheck(leakingContext, response);

            SuperSingleton.getInstance(leakingContext).getVolleyRequestQueue().add(new ChapterListRequest(API.ChaptersDB, API.RequestHeaders(), new Response.Listener<List<Chapter>>() {
                @Override
                public void onResponse(List<Chapter> response) {
                    ChapterDatabase.SaveUpdate(response);

                    errorCount = 0;
                    PrepareNotification();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    errorCount++;
                }
            }));
        }
    };
    /**
     * A listener to receive errors during the update check
     */
    private final Response.ErrorListener checkError = new Response.ErrorListener() {
        /**
         * Callback method that an error has been occurred with the
         * provided error code and optional user-readable message.
         *
         * @param error The error that caused this problem
         */
        @Override
        public void onErrorResponse(VolleyError error) {
            errorCount++;
        }
    };

    public NotificationService() {
        super("BCBComicUpdateNotificationService");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public NotificationService(String name) {
        super(name);
    }

    /**
     * Create an intent for this service with a specific action
     *
     * @param context The context to create the intent with
     * @param action  The action for the intent
     * @return The created intent
     */
    private static Intent createServiceIntent(Context context, String action) {
        Intent intent = new Intent(context, NotificationService.class);
        intent.setAction(action);
        return intent;
    }

    /**
     * Create an intent to start the notification service
     *
     * @param context The context to create the intent with
     * @return The created intent
     */
    public static Intent createStartServiceIntent(Context context) {
        return createServiceIntent(context, ACTION_START);
    }

    /**
     * Create an intent to stop the notification service
     *
     * @param context The context to create the intent with
     * @return The created intent
     */
    public static Intent createStopServiceIntent(Context context) {
        return createServiceIntent(context, ACTION_STOP);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Log.d(App.TAG, "Received a notification intent!");
            String action = intent.getAction();
            Log.d(App.TAG, "Notification action: " + action);

            if (action.equals(ACTION_START)) {
                // Add a new request to the Volley queue
                // We need to check for an update before we notify the user so we don't show a notification
                // if there's nothing to notify about
                // (who knows, maybe the buffer dried up, it happens)
                SuperSingleton
                        .getInstance(this)
                        .getVolleyRequestQueue()
                        .add(new JsonRequest<Check>(JsonRequest.Method.GET, API.CheckURI, null, this.checkListener, this.checkError) {
                            @Override
                            protected Response<Check> parseNetworkResponse(NetworkResponse response) {
                                String json = new String(response.data);
                                Check check = SuperSingleton.getInstance(leakingContext).getGsonBuilder().create().fromJson(json, Check.class);
                                DataPreferences.SaveCheck(leakingContext, check);
                                return Response.success(check, null);
                            }
                        });
            }
        } finally {
            WakefulBroadcastReceiver.completeWakefulIntent(intent);
        }
    }

    /**
     * Prepare to show a notification to the user.
     */
    private void PrepareNotification() {
        int[] updateDays = DataPreferences.getUpdateDays(this);
        int updateHour = DataPreferences.getUpdateHour(this);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        int today = calendar.get(Calendar.DAY_OF_WEEK);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        boolean showNotification = false;
        StringBuilder stringBuilder = new StringBuilder();
        for (int day : updateDays) {
            if (today == day && hour >= updateHour)
                showNotification = true;

            stringBuilder.append(day);
            stringBuilder.append(",");
        }

        final long DAY = 1000 * 60 * 60 *24;

        showNotification &= (DataPreferences.getLastNotificationTime(leakingContext) - System.currentTimeMillis() >= DAY);

        if (!showNotification) {
            String updateDaysStr = stringBuilder.toString();
            Log.d(App.TAG, "Not showing the notification: today (" + today + ") is not in the update days (" + updateDaysStr + ") or the notification has already been shown!");
            return;
        }


        double chapterNumber = DataPreferences.getLatestChapterNumber(this);
        int page = DataPreferences.getLatestPage(this);

        SuperSingleton.getInstance(this)
                .getImageLoader()
                .get(API.FormatPageUrl(this, chapterNumber, page, API.getQualitySuffix(this)), new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                        DisplayNotification(response.getBitmap());
                        DataPreferences.setLastNotificationDate(leakingContext);
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        DisplayNotification(null);
                    }
                });
    }

    /**
     * Display a notification to the user
     * This will use the settings set by the user to determine whether or not the notification should have any sound or vibration.
     *
     * @param pageBitmap The bitmap for the latest page. Optional but recommended
     */
    private void DisplayNotification(@Nullable Bitmap pageBitmap) {
        Intent intent = new Intent(this, FullscreenReaderActivity.class);
        Bundle extras = new Bundle();

        // These values are used by @Link{FullscreenReaderActivity} to determine what page is being opened
        Chapter chapter = ChapterDatabase.getLastChapter();
        extras.putParcelable(FullscreenReaderActivity.EXTRA_CHAPTER, chapter);
        extras.putInt(FullscreenReaderActivity.EXTRA_PAGE, chapter.getPageCount());

        // PendingIntent instead of a regular Intent, because the Intent will happen in the future
        PendingIntent pendingIntent;
        intent.putExtras(extras);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // I don't know how to obtain the default vibration pattern, so this will need to do
        long[] vibrationPattern = new long[]{100, 100, 100};

        // Get the ringtone from the preferences
        String ringtonePath = PreferenceManager.getDefaultSharedPreferences(this).getString("notifications_ringtone", "DEFAULT_SOUND");
        Uri ringtone;
        if (ringtonePath.equals("")) {
            //silence
            ringtone = null;
        } else {
            ringtone = Uri.parse(ringtonePath);
        }

        // Build a notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(this.getString(R.string.notification_title))
                .setContentText(this.getString(R.string.notification_description))
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_custom_notification_black)
                .setAutoCancel(true)
                .setVibrate(vibrationPattern);

        if (null != pageBitmap) {
            NotificationCompat.BigPictureStyle notificationStyle = new NotificationCompat.BigPictureStyle();
            notificationStyle.bigPicture(pageBitmap);
            builder = builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(pageBitmap));
        }

        // Don't set a sound if the user has disabled the ringtone
        if (ringtone != null)
            builder.setSound(ringtone);

        // Finally, show the notification
        Notification notification = builder.build();
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(NewChapterNotificationId, notification);
    }
}
