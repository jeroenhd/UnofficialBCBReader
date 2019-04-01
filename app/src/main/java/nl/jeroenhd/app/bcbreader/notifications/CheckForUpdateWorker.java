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
import android.text.format.Time;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageView;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.RequestFuture;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
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

import static android.icu.text.DateTimePatternGenerator.DAY;

/**
 * A job that is executed periodically to check if there are any updates
 */

public class CheckForUpdateWorker extends Worker {
    public static final String TAG = "nl.jeroenhd.app.bcbreader.notifications.CheckForUpdateWorker";

    /**
     * The (very creative) ID for the notification
     */
    private static final int NewChapterNotificationId = 0xBCB;

    public CheckForUpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static void schedule(Context context) {
        int updateHourUTC = DataPreferences.getUpdateHour(context);

        TimeZone homeZone = TimeZone.getDefault();
        TimeZone utcZone = TimeZone.getTimeZone("UTC");

        Calendar localCal = Calendar.getInstance();

        Calendar cal = new GregorianCalendar(utcZone);
        cal.setTimeInMillis(localCal.getTimeInMillis());
        cal.set(Calendar.HOUR_OF_DAY, updateHourUTC);
        cal.setTimeInMillis(cal.getTimeInMillis() - homeZone.getOffset(cal.getTimeInMillis()));
        cal.setTimeZone(homeZone);

        Time timeDate = new Time();
        timeDate.set(cal.getTimeInMillis() + homeZone.getOffset(cal.getTimeInMillis()));

        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();

        try {
            WorkInfo x = WorkManager.getInstance().getWorkInfosByTag(TAG).get().get(0);
            if (x.getState() == WorkInfo.State.RUNNING){
                return;
            }
        } catch (Exception ignored) {}

        PeriodicWorkRequest downloadRequest = new PeriodicWorkRequest
                .Builder(CheckForUpdateWorker.class, 30, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .addTag(TAG)
                .build();

        WorkManager.getInstance().enqueue(downloadRequest);
    }

    private Result downloadChapterList() {
        final Context context = this.getApplicationContext();

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
            Log.e(App.TAG, "CheckForUpdateWorker failed to get chapter list from queue. Reason will be dumped below!");
            e.printStackTrace();
        } catch (ExecutionException e) {
            Log.e(App.TAG, "CheckForUpdateWorker failed to get chapter list from queue. Reason will be dumped below!");
            e.printStackTrace();
        } catch (TimeoutException e) {
            Log.e(App.TAG, "CheckForUpdateWorker failed to get chapter list from queue. Reason will be dumped below!");
            e.printStackTrace();
        }

        return Result.retry();
    }

    /**
     * Prepare to show a notification to the user.
     */
    private Result PrepareNotification() {
        int[] updateDays = DataPreferences.getUpdateDays(getApplicationContext());
        int updateHour = DataPreferences.getUpdateHour(getApplicationContext());

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

        showNotification &= (System.currentTimeMillis() - DataPreferences.getLastNotificationTime(getApplicationContext()) >= TimeUnit.DAYS.toSeconds(1));

        if (!showNotification) {
            String updateDaysStr = stringBuilder.toString();
            Log.d(App.TAG, "Not showing the notification: today (" + today + ") is not in the update days (" + updateDaysStr + ") or the notification has already been shown!");
            return Result.success();
        }


        final double chapterNumber = DataPreferences.getLatestChapterNumber(getApplicationContext());
        final int page = DataPreferences.getLatestPage(getApplicationContext());

        Resources r = getApplicationContext().getResources();
        int maxWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 256, r.getDisplayMetrics());
        int maxHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 128, r.getDisplayMetrics());

        ImageRequest ir = new ImageRequest(API.FormatPageUrl(getApplicationContext(), chapterNumber, page, "@m"), response -> {
            DisplayNotification(response);
            DataPreferences.setLastNotificationDate(getApplicationContext());
        }, maxWidth, maxHeight, ImageView.ScaleType.FIT_START, Bitmap.Config.RGB_565, error -> DisplayNotification(null));
        SuperSingleton.getInstance(getApplicationContext())
                .getVolleyRequestQueue()
                .add(ir);

        return Result.success();
    }

    /**
     * Display a notification to the user
     * This will use the settings set by the user to determine whether or not the notification should have any sound or vibration.
     *
     * @param pageBitmap The bitmap for the latest page. Optional but recommended
     */
    private void DisplayNotification(@Nullable Bitmap pageBitmap) {
        Intent intent = new Intent(getApplicationContext(), FullscreenReaderActivity.class);
        Bundle extras = new Bundle();

        // These values are used by @Link{FullscreenReaderActivity} to determine what page is being opened
        Chapter chapter = ChapterDatabase.getLastChapter();
        extras.putParcelable(FullscreenReaderActivity.EXTRA_CHAPTER, chapter);
        extras.putInt(FullscreenReaderActivity.EXTRA_PAGE, chapter.getPageCount());

        // PendingIntent instead of a regular Intent, because the Intent will happen in the future
        PendingIntent pendingIntent;
        intent.putExtras(extras);
        pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // First element: time before starting
        // Then: on followed by off followed by on etc.
        long[] vibrationPattern = new long[]{0, 300};

        // Get the ringtone from the preferences
        String ringtonePath = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("notifications_ringtone", "DEFAULT_SOUND");
        Uri ringtone;
        if (ringtonePath == null || ringtonePath.equals("")) {
            //silence
            ringtone = null;
        } else {
            ringtone = Uri.parse(ringtonePath);
        }

        // Build a notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "page_updates")
                .setContentTitle(getApplicationContext().getString(R.string.notification_title))
                .setContentText(getApplicationContext().getString(R.string.notification_description))
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_custom_notification_black)
                .setAutoCancel(true)
                .setVibrate(vibrationPattern);

        if (null != pageBitmap) {
            NotificationCompat.BigPictureStyle notificationStyle = new NotificationCompat.BigPictureStyle();
            notificationStyle.setBigContentTitle(getApplicationContext().getString(R.string.notification_title));
            notificationStyle.setSummaryText(getApplicationContext().getString(R.string.notification_description));
            notificationStyle.bigPicture(pageBitmap);
            builder = builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(pageBitmap));
        }

        // Don't set a sound if the user has disabled the ringtone
        if (ringtone != null)
            builder.setSound(ringtone);

        // Finally, show the notification
        Notification notification = builder.build();
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        notificationManagerCompat.notify(NewChapterNotificationId, notification);
    }

    @androidx.annotation.NonNull
    @Override
    public Result doWork() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        JobOnBackgroundRunnable r = new JobOnBackgroundRunnable(countDownLatch);
        new Thread(r).start();

        try {
            countDownLatch.await();
            return r.getResult() == null ? Result.retry() : r.getResult();
        } catch (InterruptedException e) {
            return Result.failure();
        }
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

            final Context context = getApplicationContext();

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

                return;
            } catch (InterruptedException e) {
                Log.e(App.TAG, "CheckForUpdateWorker failed to get Check object from queue. Reason will be dumped below!");
                e.printStackTrace();
            } catch (ExecutionException e) {
                Log.e(App.TAG, "CheckForUpdateWorker failed to get Check object from queue. Reason will be dumped below!");
                e.printStackTrace();
            } catch (TimeoutException e) {
                Log.e(App.TAG, "CheckForUpdateWorker failed to get Check object from queue. Reason will be dumped below!");
                e.printStackTrace();
            }
            // Reschedule after any failure
            result = Result.retry();
        }

        Result getResult() {
            return result;
        }
    }
}
