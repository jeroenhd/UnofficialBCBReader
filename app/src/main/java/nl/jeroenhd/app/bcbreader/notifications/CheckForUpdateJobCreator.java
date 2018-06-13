package nl.jeroenhd.app.bcbreader.notifications;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;
import com.evernote.android.job.JobRequest;

import java.util.concurrent.TimeUnit;

import nl.jeroenhd.app.bcbreader.data.App;

public class CheckForUpdateJobCreator implements JobCreator {
    public static void scheduleJob() {
        new JobRequest.Builder(CheckForUpdateJob.TAG)
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .setPeriodic(TimeUnit.MINUTES.convert(15, TimeUnit.MILLISECONDS))
                .build()
                .scheduleAsync();
    }

    @Nullable
    @Override
    public Job create(@NonNull String tag) {
        switch (tag) {
            case CheckForUpdateJob.TAG:
                return new CheckForUpdateJob();
            default:
                Log.w(App.TAG, "Invalid JobCreator tag " + tag + "; unknown ID!");
                return null;
        }
    }
}
