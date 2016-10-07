package nl.jeroenhd.app.bcbreader.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonRequest;

import java.util.List;

import nl.jeroenhd.app.bcbreader.ChapterReadingActivity;
import nl.jeroenhd.app.bcbreader.R;
import nl.jeroenhd.app.bcbreader.data.API;
import nl.jeroenhd.app.bcbreader.data.Chapter;
import nl.jeroenhd.app.bcbreader.data.ChapterListRequest;
import nl.jeroenhd.app.bcbreader.data.SuperSingleton;
import nl.jeroenhd.app.bcbreader.data.check.Check;
import nl.jeroenhd.app.bcbreader.data.databases.ChapterDatabase;

/**
 * Handles the alarm to check for updates
 */
public class UpdateBroadcastReceiver extends BroadcastReceiver {
    public static final int NewChapterNotificationId = 0xB00B;
    public static final String UPDATE = "nl.jeroenhd.app.bcbreader.notification.UPDATE";

    private int errorCount;
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
    private Context mContext;

    private final Response.Listener<Check> checkListener = new Response.Listener<Check>() {
        @Override
        public void onResponse(Check response) {
            SuperSingleton.getInstance(mContext).getVolleyRequestQueue().add(new ChapterListRequest(API.ChaptersDB, API.RequestHeaders(), new Response.Listener<List<Chapter>>() {
                @Override
                public void onResponse(List<Chapter> response) {
                    ChapterDatabase.SaveUpdate(response);

                    errorCount = 0;
                    DisplayNotification();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    errorCount++;
                }
            }));
        }
    };

    private void DisplayNotification() {
        Intent intent = new Intent(mContext, ChapterReadingActivity.class);
        Bundle extras = new Bundle();

        Chapter chapter = ChapterDatabase.getLastChapter();

        extras.putParcelable(ChapterReadingActivity.CHAPTER, chapter);
        extras.putInt(ChapterReadingActivity.SCROLL_TO, chapter.getPageCount());

        PendingIntent pendingIntent;
        intent.putExtras(extras);
        pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notification = new NotificationCompat.Builder(mContext)
                .setContentTitle(mContext.getString(R.string.notification_title))
                .setContentText(mContext.getString(R.string.notification_description))
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_custom_notification_black)
                .build();
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(mContext);
        notificationManagerCompat.notify(NewChapterNotificationId, notification);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;

        String action = intent.getAction();

        if (action == null) {
            Log.e("UpdateOnReceive", "Somehow a null intent was received");
        }

        if (!action.equals(Intent.ACTION_BOOT_COMPLETED) && !action.equals(UPDATE)) {
            Log.e("UpdateOnReceive", "Invalid action was passed to the UpdateBroadcastReceiver (why did it trigger???)");
            return;
        }

        SuperSingleton.getInstance(mContext).getVolleyRequestQueue().add(new JsonRequest<Check>(JsonRequest.Method.GET, API.CheckURI, null, this.checkListener, this.checkError) {
            @Override
            protected Response<Check> parseNetworkResponse(NetworkResponse response) {
                String json = new String(response.data);
                return Response.success(SuperSingleton.getInstance(mContext).getGsonBuilder().create().fromJson(json, Check.class), null);
            }
        });
    }
}
