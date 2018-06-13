package nl.jeroenhd.app.bcbreader.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageView;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.RequestFuture;
import com.evernote.android.job.Job;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import nl.jeroenhd.app.bcbreader.R;
import nl.jeroenhd.app.bcbreader.activities.FullscreenReaderActivity;
import nl.jeroenhd.app.bcbreader.data.API;
import nl.jeroenhd.app.bcbreader.data.App;
import nl.jeroenhd.app.bcbreader.data.Chapter;
import nl.jeroenhd.app.bcbreader.data.ChapterListDeserializer;
import nl.jeroenhd.app.bcbreader.data.Page;
import nl.jeroenhd.app.bcbreader.data.SuperSingleton;
import nl.jeroenhd.app.bcbreader.data.check.Check;
import nl.jeroenhd.app.bcbreader.data.check.DataPreferences;
import nl.jeroenhd.app.bcbreader.data.databases.ChapterDatabase;

/**
 * A job that is executed periodically to check if there are any updates
 */

public class CheckForUpdateJob extends Job {
    public static final String TAG = "nl.jeroenhd.app.bcbreader.notifications.CheckForUpdateJob";

    /**
     * The (very creative) ID for the notification
     */
    private static final int NewChapterNotificationId = 0xB00B;

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        Log.v(App.TAG, "Starting a CheckForUpdate job!");
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        JobOnBackgroundRunnable r = new JobOnBackgroundRunnable(countDownLatch);
        new Thread(r).start();

        try {
            Log.v(App.TAG, "Awaiting countdown latch...");
            countDownLatch.await();
            Result result = r.getResult();
            Log.v(App.TAG, "Latch triggered! Result: " + (result == null ? "NULL" : result.toString()));
            return result == null ? Result.RESCHEDULE : r.getResult();
        } catch (InterruptedException e) {
            Log.e(App.TAG, "Failed to wait for latch; rescheduling!");
            e.printStackTrace();
            return Result.RESCHEDULE;
        }
    }

    private Result downloadChapterList() {
        final Context context = this.getContext();

        RequestFuture<List<Chapter>> future = RequestFuture.newFuture();

        JsonRequest<List<Chapter>> jsonRequest = new JsonRequest<List<Chapter>>(JsonRequest.Method.GET, API.ChaptersDB, null, future, future) {
            @Override
            protected Response<List<Chapter>> parseNetworkResponse(NetworkResponse response) {
                try {
                    // Build a new Gson
                    GsonBuilder builder = new GsonBuilder();
                    // Prepare a valid list
                    List<Chapter> chapterList = new ArrayList<>();
                    // Have Gson use out ChapterListDeserializer for the chapter list
                    builder.registerTypeAdapter(chapterList.getClass(), new ChapterListDeserializer());

                    // Create the usable Gson object
                    Gson gson = builder
                            .excludeFieldsWithoutExposeAnnotation()
                            .create();

                    // Get the downloaded JSON
                    String json = new String(
                            response.data,
                            Charset.forName("Windows-1252"));

                    // Decode the JSON
                    //noinspection unchecked
                    chapterList = gson.fromJson(json, chapterList.getClass());
                    for (Chapter c : chapterList) {
                        for (Page p : c.pageDescriptions) {
                            p.setChapter(c.getNumber());
                        }
                    }

                    return Response.success(
                            chapterList,
                            HttpHeaderParser.parseCacheHeaders(response));
                } catch (JsonSyntaxException e) {
                    // Bad JSON!
                    return Response.error(new ParseError(e));
                }
            }
        };

        Log.v(App.TAG, "Queuing chapter list download...");
        SuperSingleton.getInstance(context)
                .getVolleyRequestQueue()
                .add(jsonRequest);


        try {
            Log.v(App.TAG, "Waiting up to 30 seconds for the chapter list to download...");
            List<Chapter> chapters = future.get(30, TimeUnit.SECONDS);
            Log.v(App.TAG, "Saving new chapter list...");
            ChapterDatabase.SaveUpdate(chapters);

            return PrepareNotification();
        } catch (InterruptedException e) {
            Log.e(App.TAG, "CheckForUpdateJob failed to get chapter list from queue. Reason will be dumped below!");
            e.printStackTrace();
        } catch (ExecutionException e) {
            Log.e(App.TAG, "CheckForUpdateJob failed to get chapter list from queue. Reason will be dumped below!");
            e.printStackTrace();
        } catch (TimeoutException e) {
            Log.e(App.TAG, "CheckForUpdateJob failed to get chapter list from queue. Reason will be dumped below!");
            e.printStackTrace();
        }

        return Result.SUCCESS;
    }

    /**
     * Prepare to show a notification to the user.
     */
    private Result PrepareNotification() {
        Log.v(App.TAG, "Preparing notification...");
        int[] updateDays = DataPreferences.getUpdateDays(getContext());
        int updateHour = DataPreferences.getUpdateHour(getContext());

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        int today = calendar.get(Calendar.DAY_OF_WEEK);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        Log.v(App.TAG, "Checking if a new notification is needed...");
        boolean showNotification = false;
        StringBuilder stringBuilder = new StringBuilder();
        for (int day : updateDays) {
            if (today == day && hour >= updateHour) {
                showNotification = true;
                Log.v(App.TAG, "Notification is required! today == day && hour >= updateHour");
            }

            stringBuilder.append(day);
            stringBuilder.append(",");
        }

        // This messes with the notification handling

        //final long DAY = TimeUnit.DAYS.toMillis(1);
        //showNotification &= (System.currentTimeMillis() - DataPreferences.getLastNotificationTime(getContext()) >= DAY);

        if (!showNotification) {
            String updateDaysStr = stringBuilder.toString();
            Log.w(App.TAG, "Not showing the notification: today (" + today + ") is not in the update days (" + updateDaysStr + ") or the notification has already been shown!");
            return Result.SUCCESS;
        }


        final double chapterNumber = DataPreferences.getLatestChapterNumber(getContext());
        final int page = DataPreferences.getLatestPage(getContext());
        Log.i(App.TAG, "Latest chapter: " + chapterNumber + ", latest page: " + page);

        Log.v(App.TAG, "Preparing to resize comic page...");
        Resources r = getContext().getResources();
        int maxWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 256, r.getDisplayMetrics());
        int maxHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 128, r.getDisplayMetrics());

        ImageRequest ir = new ImageRequest(API.FormatPageUrl(getContext(), chapterNumber, page, "@m"), new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                Log.v(App.TAG, "Download successful; showing notification!");
                DisplayNotification(response);
                DataPreferences.setLastNotificationDate(getContext());
            }
        }, maxWidth, maxHeight, ImageView.ScaleType.FIT_START, Bitmap.Config.RGB_565, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v(App.TAG, "Download failed; showing notification without comic page!");
                DisplayNotification(null);
            }
        });
        Log.v(App.TAG, "Downloading latest page...");
        SuperSingleton.getInstance(getContext())
                .getVolleyRequestQueue()
                .add(ir);

        Log.v(App.TAG, "Seemes like everything turned out right in the end");
        return Result.SUCCESS;
    }

    /**
     * Display a notification to the user
     * This will use the settings set by the user to determine whether or not the notification should have any sound or vibration.
     *
     * @param pageBitmap The bitmap for the latest page. Optional but recommended
     */
    private void DisplayNotification(@Nullable Bitmap pageBitmap) {
        Intent intent = new Intent(getContext(), FullscreenReaderActivity.class);
        Bundle extras = new Bundle();

        // These values are used by @Link{FullscreenReaderActivity} to determine what page is being opened
        Chapter chapter = ChapterDatabase.getLastChapter();
        extras.putParcelable(FullscreenReaderActivity.EXTRA_CHAPTER, chapter);
        extras.putInt(FullscreenReaderActivity.EXTRA_PAGE, chapter.getPageCount());

        // PendingIntent instead of a regular Intent, because the Intent will happen in the future
        PendingIntent pendingIntent;
        intent.putExtras(extras);
        pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // First element: time before starting
        // Then: on followed by off followed by on etc.
        long[] vibrationPattern = new long[]{0, 300};

        // Get the ringtone from the preferences
        String ringtonePath = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("notifications_ringtone", "DEFAULT_SOUND");
        Uri ringtone;
        if (ringtonePath.equals("")) {
            //silence
            ringtone = null;
        } else {
            ringtone = Uri.parse(ringtonePath);
        }

        // Build a notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), "page_updates")
                .setContentTitle(getContext().getString(R.string.notification_title))
                .setContentText(getContext().getString(R.string.notification_description))
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_custom_notification_black)
                .setAutoCancel(true)
                .setVibrate(vibrationPattern);

        if (null != pageBitmap) {
            NotificationCompat.BigPictureStyle notificationStyle = new NotificationCompat.BigPictureStyle();
            notificationStyle.setBigContentTitle(getContext().getString(R.string.notification_title));
            notificationStyle.setSummaryText(getContext().getString(R.string.notification_description));
            notificationStyle.bigPicture(pageBitmap);
            builder = builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(pageBitmap));
        }

        // Don't set a sound if the user has disabled the ringtone
        if (ringtone != null)
            builder.setSound(ringtone);

        // Finally, show the notification
        Notification notification = builder.build();
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getContext());
        notificationManagerCompat.notify(NewChapterNotificationId, notification);
    }

    private class JobOnBackgroundRunnable implements Runnable {
        volatile Result result;
        private CountDownLatch countDownLatch;

        JobOnBackgroundRunnable(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            RequestFuture<Check> future = RequestFuture.newFuture();

            final Context context = getContext();

            JsonRequest<Check> jsonRequest = new JsonRequest<Check>(JsonRequest.Method.GET, API.CheckURI, null, future, future) {
                @Override
                protected Response<Check> parseNetworkResponse(NetworkResponse response) {
                    String json = new String(response.data);
                    Log.v(App.TAG, "Checked for updates in background! Response: " + json);
                    Check check = SuperSingleton.getInstance(context).getGsonBuilder().create().fromJson(json, Check.class);
                    DataPreferences.SaveCheck(context, check);
                    return Response.success(check, null);
                }
            };

            Log.v(App.TAG, "Queueing background check...");
            SuperSingleton.getInstance(context)
                    .getVolleyRequestQueue()
                    .add(jsonRequest);

            try {
                Log.v(App.TAG, "Waiting up to 30 seconds for the check to be executed...");
                Check check = future.get(30, TimeUnit.SECONDS);
                Log.v(App.TAG, "Check executed!");

                DataPreferences.SaveCheck(context, check);

                Log.v(App.TAG, "Now downloading the new chapter list...");
                result = downloadChapterList();

                Log.v(App.TAG, "Counting down latch...");
                countDownLatch.countDown();
            } catch (InterruptedException e) {
                Log.e(App.TAG, "CheckForUpdateJob failed to get Check object from queue. Reason will be dumped below!");
                e.printStackTrace();
            } catch (ExecutionException e) {
                Log.e(App.TAG, "CheckForUpdateJob failed to get Check object from queue. Reason will be dumped below!");
                e.printStackTrace();
            } catch (TimeoutException e) {
                Log.e(App.TAG, "CheckForUpdateJob failed to get Check object from queue. Reason will be dumped below!");
                e.printStackTrace();
            }

            result = Result.SUCCESS;
        }

        Result getResult() {
            return result;
        }
    }
}
