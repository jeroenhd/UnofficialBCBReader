package nl.jeroenhd.app.bcbreader.notifications;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

import nl.jeroenhd.app.bcbreader.data.App;

/**
 * Create jobs for the notification system
 */

public class NotificationJobCreator implements JobCreator {
    public NotificationJobCreator() {
        super();
    }

    @Nullable
    @Override
    public Job create(@NonNull String tag) {
        switch (tag) {
            case CheckForUpdateJob.TAG:
                Log.v(App.TAG, "Starting check for update");
                return new CheckForUpdateJob();
            default:
                Log.d(App.TAG, "Invalid job was passed to NotificationJobCreator: " + tag);
                return null;
        }
    }
}
