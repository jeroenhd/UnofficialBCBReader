package nl.jeroenhd.app.bcbreader;

import android.app.Application;

import com.raizlabs.android.dbflow.config.FlowManager;

/**
 * Custom application object for DBFlow
 */
public class BCBReaderApplication extends Application {
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
        FlowManager.init(this);
    }
}
