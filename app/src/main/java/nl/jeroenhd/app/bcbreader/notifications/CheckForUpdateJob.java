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
import com.evernote.android.job.DailyJob;
import com.evernote.android.job.JobRequest;
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

public class CheckForUpdateJob extends DailyJob {
    public static final String TAG = "nl.jeroenhd.app.bcbreader.notifications.CheckForUpdateJob";

    /**
     * The (very creative) ID for the notification
     */
    private static final int NewChapterNotificationId = 0xB00B;

    public static void scheduleJob() {

        long intervalMs = TimeUnit.MINUTES.toMillis(30);
        long flexMs = TimeUnit.MINUTES.toMillis(5);
        new JobRequest.Builder(CheckForUpdateJob.TAG)
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .setPeriodic(intervalMs, flexMs)
                .build()
                .schedule();
    }

    /**
     * Get the local update hour
     *
     * @param context A context to use for getting preferences
     * @return The hour of day that the app should check for updates
     */
    private static long getUpdateHour(Context context) {
        int updateHourUTC = DataPreferences.getUpdateHour(context);
        Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, updateHourUTC);
        c.setTimeZone(TimeZone.getDefault());
        return c.get(Calendar.HOUR_OF_DAY);
    }

    public static void schedule(Context context) {
        DailyJob.schedule(
                new JobRequest.Builder(App.TAG),
                TimeUnit.HOURS.toMillis(getUpdateHour(context)),
                TimeUnit.HOURS.toMillis(getUpdateHour(context)) + TimeUnit.MINUTES.toMillis(15));
    }

    @NonNull
    @Override
    protected DailyJobResult onRunDailyJob(Params params) {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        JobOnBackgroundRunnable r = new JobOnBackgroundRunnable(countDownLatch);
        new Thread(r).start();

        try {
            countDownLatch.await();
            return r.getResult() == null ? DailyJobResult.SUCCESS : r.getResult();
        } catch (InterruptedException e) {
            return DailyJobResult.SUCCESS;
        }
    }

    private DailyJobResult downloadChapterList() {
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

        SuperSingleton.getInstance(context)
                .getVolleyRequestQueue()
                .add(jsonRequest);


        try {
            List<Chapter> chapters = future.get(30, TimeUnit.SECONDS);
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

        return DailyJobResult.SUCCESS;
    }

    /**
     * Prepare to show a notification to the user.
     */
    private DailyJobResult PrepareNotification() {
        int[] updateDays = DataPreferences.getUpdateDays(getContext());
        int updateHour = DataPreferences.getUpdateHour(getContext());

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

        final long DAY = TimeUnit.DAYS.toMillis(1);

        showNotification &= (System.currentTimeMillis() - DataPreferences.getLastNotificationTime(getContext()) >= DAY);

        if (!showNotification) {
            String updateDaysStr = stringBuilder.toString();
            Log.w(App.TAG, "Not showing the notification: today (" + today + ") is not in the update days (" + updateDaysStr + ") or the notification has already been shown!");
            return DailyJobResult.SUCCESS;
        }


        final double chapterNumber = DataPreferences.getLatestChapterNumber(getContext());
        final int page = DataPreferences.getLatestPage(getContext());

        Resources r = getContext().getResources();
        int maxWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 256, r.getDisplayMetrics());
        int maxHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 128, r.getDisplayMetrics());

        ImageRequest ir = new ImageRequest(API.FormatPageUrl(getContext(), chapterNumber, page, "@m"), new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                DisplayNotification(response);
                DataPreferences.setLastNotificationDate(getContext());
            }
        }, maxWidth, maxHeight, ImageView.ScaleType.FIT_START, Bitmap.Config.RGB_565, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                DisplayNotification(null);
            }
        });
        SuperSingleton.getInstance(getContext())
                .getVolleyRequestQueue()
                .add(ir);

        return DailyJobResult.SUCCESS;
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
        volatile DailyJobResult result;
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
                    Check check = SuperSingleton.getInstance(context).getGsonBuilder().create().fromJson(json, Check.class);
                    DataPreferences.SaveCheck(context, check);
                    return Response.success(check, null);
                }
            };

            SuperSingleton.getInstance(context)
                    .getVolleyRequestQueue()
                    .add(jsonRequest);

            try {
                Check check = future.get(30, TimeUnit.SECONDS);

                DataPreferences.SaveCheck(context, check);

                result = downloadChapterList();
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

            result = DailyJobResult.SUCCESS;
        }

        DailyJobResult getResult() {
            return result;
        }
    }
}
