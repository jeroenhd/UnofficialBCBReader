package nl.jeroenhd.app.bcbreader.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

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
    int errorCount;
    Context mContext;
    private Response.Listener<Check> checkListener = new Response.Listener<Check>() {
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
    private Response.ErrorListener checkError = new Response.ErrorListener() {
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

    public void DisplayNotification() {
        Intent intent = new Intent(mContext, ChapterReadingActivity.class);
        Bundle extras = new Bundle();

        Chapter chapter = ChapterDatabase.getLastChapter();

        extras.putParcelable(ChapterReadingActivity.CHAPTER, chapter);

        PendingIntent pendingIntent = null;
        intent.putExtras(extras);
        pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notification = new NotificationCompat.Builder(mContext)
                .setContentTitle(mContext.getString(R.string.notification_title))
                .setContentText(mContext.getString(R.string.notification_description))
                .setContentIntent(pendingIntent)
                .build();
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(mContext);
        notificationManagerCompat.notify(NewChapterNotificationId, notification);
    }

    /**
     * This method is called when the BroadcastReceiver is receiving an Intent
     * broadcast.  During this time you can use the other methods on
     * BroadcastReceiver to view/modify the current result values.  This method
     * is always called within the main thread of its process, unless you
     * explicitly asked for it to be scheduled on a different thread using
     * Context#registerReceiver(BroadcastReceiver,
     * IntentFilter, String, Handler). When it runs on the main
     * thread you should
     * never perform long-running operations in it (there is a timeout of
     * 10 seconds that the system allows before considering the receiver to
     * be blocked and a candidate to be killed). You cannot launch a popup dialog
     * in your implementation of onReceive().
     * <p/>
     * <p><b>If this BroadcastReceiver was launched through a &lt;receiver&gt; tag,
     * then the object is no longer alive after returning from this
     * function.</b>  This means you should not perform any operations that
     * return a result to you asynchronously -- in particular, for interacting
     * with services, you should use
     * {@link Context#startService(Intent)} instead of
     * {@link Context#bindService(Intent, ServiceConnection, int)}.  If you wish
     * to interact with a service that is already running, you can use
     * {@link #peekService}.
     * <p/>
     * <p>The Intent filters used in {@link Context#registerReceiver}
     * and in application manifests are <em>not</em> guaranteed to be exclusive. They
     * are hints to the operating system about how to find suitable recipients. It is
     * possible for senders to force delivery to specific recipients, bypassing filter
     * resolution.  For this reason, {@link #onReceive(Context, Intent) onReceive()}
     * implementations should respond only to known actions, ignoring any unexpected
     * Intents that they may receive.
     *
     * @param context The Context in which the receiver is running.
     * @param intent  The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;

        SuperSingleton.getInstance(mContext).getVolleyRequestQueue().add(new JsonRequest<Check>(JsonRequest.Method.GET, API.CheckURI, null, this.checkListener, this.checkError) {
            @Override
            protected Response<Check> parseNetworkResponse(NetworkResponse response) {
                String json = new String(response.data);
                return Response.success(SuperSingleton.getInstance(mContext).getGsonBuilder().create().fromJson(json, Check.class), null);
            }
        });
    }
}
