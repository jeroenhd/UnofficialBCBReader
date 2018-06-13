package nl.jeroenhd.app.bcbreader;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.evernote.android.job.JobManager;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;

import nl.jeroenhd.app.bcbreader.notifications.CheckForUpdateJobCreator;
import nl.jeroenhd.app.bcbreader.tools.AppCrashStorage;
import nl.jeroenhd.app.bcbreader.tools.Shortcuts;

import static nl.jeroenhd.app.bcbreader.data.App.TAG;

/**
 * Custom application object for DBFlow
 */
public class BCBReaderApplication extends Application {
    public static final String ACTION_SHORTCUT = "nl.jeroenhd.app.bcbreader.ACTION_SHORTCUT";
    private Thread.UncaughtExceptionHandler systemHandler;

    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     * Implementations should be as quick as possible (for example using
     * lazy initialization of state) since the time spent in this function
     * directly impacts the performance of starting the first activity,
     * service, or receiver in a process.
     * If you override this method, be sure to call super.onCreate().
     */
    @Override
    public void onCreate() {
        super.onCreate();

        this.initCrashReporting();

        FlowManager.init(
                new FlowConfig.Builder(this).build()
        );

        // Prepare notifications
        this.prepareNotifications();
        JobManager.create(this).addJobCreator(new CheckForUpdateJobCreator());
        CheckForUpdateJobCreator.scheduleJob();

        Shortcuts.Update(this);
    }

    /**
     * Prepare the notification system for Android Oreo and up
     * See also: https://developer.android.com/guide/topics/ui/notifiers/notifications.html#ManageChannels
     */
    private void prepareNotifications()
    {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // The id of the channel.
        String id = "page_updates";

        // The user-visible name of the channel.
        CharSequence name = getString(R.string.channel_name);

        // The user-visible description of the channel.
        String description = getString(R.string.channel_description);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(id, name, importance);
        // Configure the notification channel.
        mChannel.setDescription(description);
        mChannel.enableLights(true);

        // Sets the notification light color for notifications posted to this
        // channel, if the device supports this feature.
        mChannel.setLightColor(Color.MAGENTA);
        mChannel.enableVibration(true);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        if (mNotificationManager != null) {
            mNotificationManager.createNotificationChannel(mChannel);
        } else {
            Log.e(TAG, "Failed to create notification channel; notifications may not behave normally!");
        }

    }

    /**
     * Set up a crash report system.
     */
    private void initCrashReporting() {
        systemHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                Log.e(TAG, "Unhandled exception! The global crash handler has been invoked!");

                AppCrashStorage appCrashStorage = new AppCrashStorage(BCBReaderApplication.this);
                appCrashStorage.StoreCrash(thread, throwable);

                if (systemHandler != null) {
                    systemHandler.uncaughtException(thread, throwable);
                }
            }
        });
    }
}
