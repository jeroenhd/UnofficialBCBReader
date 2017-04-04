package nl.jeroenhd.app.bcbreader;

import android.app.Application;
import android.util.Log;

import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;

import nl.jeroenhd.app.bcbreader.broadcast_receivers.UpdateEventReceiver;
import nl.jeroenhd.app.bcbreader.data.App;
import nl.jeroenhd.app.bcbreader.tools.AppCrashStorage;
import nl.jeroenhd.app.bcbreader.tools.Shortcuts;

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

        UpdateEventReceiver.setupAlarm(this);
        Shortcuts.Update(this);
    }

    /**
     * Set up a crash report system.
     */
    private void initCrashReporting() {
        systemHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                Log.e(App.TAG, "Unhandled exception! The global crash handler has been invoked!");

                AppCrashStorage appCrashStorage = new AppCrashStorage(BCBReaderApplication.this);
                appCrashStorage.StoreCrash(thread, throwable);

                if (systemHandler != null) {
                    systemHandler.uncaughtException(thread, throwable);
                }
            }
        });
    }
}
