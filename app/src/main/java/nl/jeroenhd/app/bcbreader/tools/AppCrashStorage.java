package nl.jeroenhd.app.bcbreader.tools;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
    private static final int CRASH_REPORT_FORMAT_VERSION = 1;
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
            String fileName = System.currentTimeMillis() + "-" + App.Version() + "-" + crashedThread.getName() + ".json";
            File outputFile = new File(crashDirectory, fileName);

            // Add the report version (just in case)
            JsonObject innerObject = new JsonObject();
            innerObject.addProperty("reportVersion", CRASH_REPORT_FORMAT_VERSION);

            // Add some info about the app
            JsonObject appInfoObject = new JsonObject();
            appInfoObject.addProperty("appVersion", BuildConfig.VERSION_CODE);
            appInfoObject.addProperty("appVersionName", BuildConfig.VERSION_NAME);
            innerObject.add("app", appInfoObject);

            // Add basic device information
            Telemetry deviceInformation = Telemetry.getInstance(context);
            JsonObject deviceInfoObject = new JsonObject();
            deviceInfoObject.addProperty("model", deviceInformation.getModel());
            deviceInfoObject.addProperty("androidVersion", deviceInformation.getAndroidVersion());
            innerObject.add("device", deviceInfoObject);

            // Add thread and stack trace information
            JsonObject crashInfoObject = new JsonObject();
            crashInfoObject.addProperty("threadName", crashedThread.getName());
            crashInfoObject.addProperty("exceptionType", crashThrowable.getClass().getCanonicalName());

            // Get stack trace
            crashInfoObject.addProperty("stacktrace", Log.getStackTraceString(crashThrowable));
            innerObject.add("crash", crashInfoObject);

            // List all granted permissions
            JsonArray permissionsObject = new JsonArray();
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
            for (int i = 0; i < pi.requestedPermissions.length; i++) {
                if ((pi.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0) {
                    permissionsObject.add(pi.requestedPermissions[i]);
                }
            }
            innerObject.add("permissions", permissionsObject);

            // Open, write, flush and close crash log file
            PrintWriter printWriter = new PrintWriter(outputFile);
            printWriter.write(innerObject.toString());
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

    /**
     * List all crash files
     *
     * @return An array of files in the crash directory
     */
    public File[] getCrashFiles() {
        File crashDirectory = context.getDir(CRASH_DIRECTORY, 0);
        return crashDirectory.listFiles();
    }

    /**
     * Send crash reports
     */
    public void send() {
        for (File crashReportFile : this.getCrashFiles()) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(crashReportFile));

                StringBuilder builder = new StringBuilder();
                String tmpString;
                while (null != (tmpString = reader.readLine())) {
                    builder.append(tmpString);
                }
                String JSON = builder.toString();

                JSONObject crashReportObject = new JSONObject(JSON);
                JsonObjectRequest request = new JsonObjectRequest("https://app.jeroenhd.nl/bcb/crashReport.php", crashReportObject, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //Ignored
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Ignored
                    }
                });
            } catch (Exception e) {
                // IGNORE
            }

        }
    }

    /**
     * Delete all crash reports
     */
    public void deleteReports() {
        for (File crashReport : this.getCrashFiles()) {
            // If the delete failed somehow we can't do much about it...
            //noinspection ResultOfMethodCallIgnored
            crashReport.delete();
        }
    }
}
