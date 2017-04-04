package nl.jeroenhd.app.bcbreader.tools;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.File;
import java.io.PrintWriter;

import nl.jeroenhd.app.bcbreader.BuildConfig;
import nl.jeroenhd.app.bcbreader.data.App;
import nl.jeroenhd.app.bcbreader.data.Telemetry;

/**
 * A class to store and retrieve crash data
 */
public class AppCrashStorage {
    /**
     * The app data directory to store all crash logs in
     */
    private static final String CRASH_DIRECTORY = "sunken_ships";
    private static final String CRASH_LOG_HEADER = "This is a crash log for the unofficial BCB Reader\nNo personal information is present in this file!\n";
    /**
     * The context used to get file access
     */
    private Context context;

    /**
     * Create a new AppCrashStorage object
     *
     * @param context The context used to get file access
     */
    public AppCrashStorage(Context context) {
        this.context = context;
    }

    /**
     * Store a crash log in a file so it can be sent next time
     *
     * @param crashedThread  The crashed thread
     * @param crashThrowable The exception that was not caught
     */
    public void StoreCrash(Thread crashedThread, Throwable crashThrowable) {
        // If this crashes as well... then all hope is lost!
        // Just put a try/catch handler around this code that doesn't do much
        try {
            File crashDirectory = context.getDir(CRASH_DIRECTORY, 0);
            String fileName = System.currentTimeMillis() + "-" + App.Version() + "-" + crashedThread.getName() + ".crash";
            File outputFile = new File(crashDirectory, fileName);
            PrintWriter printWriter = new PrintWriter(outputFile);

            // Write basic device info
            Telemetry deviceInformation = Telemetry.getInstance(context);
            printWriter.write(CRASH_LOG_HEADER);
            printWriter.write("App version number: " + BuildConfig.VERSION_CODE +
                    "\nFull app version: " + BuildConfig.VERSION_NAME + "======\n");
            printWriter.write("Basic device information:\nModel: " + deviceInformation.getModel()
                    + "\nAndroid version: " + deviceInformation.getAndroidVersion() + "\n======\n");


            // Write crash log
            printWriter.write("Crashed thread: " + crashedThread.getName() + "\n");
            printWriter.write("Stack trace:\n");
            crashThrowable.printStackTrace(printWriter);
            printWriter.write("======\n");
            // Flush just to be sure (the code below can trigger an exception)
            printWriter.flush();

            // Write permissions
            printWriter.write("Granted permissions:\n");
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
            for (int i = 0; i < pi.requestedPermissions.length; i++) {
                if ((pi.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0) {
                    printWriter.write(pi.requestedPermissions[i] + "\n");
                }
            }

            // Flush and close file
            printWriter.flush();
            printWriter.close();
        } catch (Exception e) {
            Log.e(App.TAG, "An exception occurred processing an uncaught error! Some information might not have been written to disk!");
            Log.e(App.TAG, "This happened:");
            e.printStackTrace();
            Log.e(App.TAG, "While processing this exception:");
            crashThrowable.printStackTrace();
        }
    }
}
