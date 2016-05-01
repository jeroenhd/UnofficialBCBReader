package nl.jeroenhd.app.bcbreader.data;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;

import nl.jeroenhd.app.bcbreader.R;

/**
 * Class to detect telemetry
 */
public class Telemetry {
    static final String TelemetryURL = "https://www.jeroenhd.nl/proj/app/androidTelemetry.php";
    protected static Telemetry instance;
    protected Context mContext;
    String Model, AndroidVersion;
    long InternalSize, InternalFree;
    long SDCardSize, SDCardFree;
    long RAMSize;
    boolean SDCardEmulated;
    String uniqueID;

    protected Telemetry(Context context) {
        mContext = context;

        Model = Build.MODEL;
        AndroidVersion = Build.VERSION.RELEASE;

        // External storage available (could ebe emulated)
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            SDCardEmulated = Environment.isExternalStorageEmulated();
            SDCardSize = getVolumeSizeByPath(Environment.getExternalStorageDirectory());
            SDCardFree = getVolumeFreeByPath(Environment.getExternalStorageDirectory());
        }
        InternalSize = getVolumeSizeByPath(Environment.getDataDirectory());
        InternalFree = getVolumeFreeByPath(Environment.getDataDirectory());

        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            RAMSize = memoryInfo.totalMem;
        } else {
            RAMSize = 0;
        }

        // Get the android ID, but HASH IT FIRST!
        // We don't need the unique ID itself, the hash code should be unique enough
        uniqueID = "V1[" + Integer.toHexString(Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID).hashCode()) + "]";
    }

    public static Telemetry getInstance(Context context) {
        if (null == instance)
            instance = new Telemetry(context);

        return instance;
    }

    protected long getVolumeSizeByPath(File path) {
        StatFs stat = new StatFs(path.toString());
        long blockSize, blockCount;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = stat.getBlockSizeLong();
            blockCount = stat.getBlockCountLong();
        } else {
            blockSize = stat.getBlockSize();
            blockCount = stat.getBlockCount();
        }
        return blockCount * blockSize;
    }

    protected long getVolumeFreeByPath(File path) {
        StatFs stat = new StatFs(path.toString());
        long blockSize, blockCount;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = stat.getBlockSizeLong();
            blockCount = stat.getAvailableBlocksLong();
        } else {
            blockSize = stat.getBlockSize();
            blockCount = stat.getAvailableBlocks();
        }
        return blockSize * blockCount;
    }

    public String getModel() {
        return Model;
    }

    public String getAndroidVersion() {
        return AndroidVersion;
    }

    public long getInternalSize() {
        return InternalSize;
    }

    public long getInternalFree() {
        return InternalFree;
    }

    public long getSDCardSize() {
        return SDCardSize;
    }

    public long getSDCardFree() {
        return SDCardFree;
    }

    public long getInternalSizeMB() {
        return getInternalSize() / (1024 * 1024);
    }

    public long getInternalFreeMB() {
        return getInternalFree() / (1024 * 1024);
    }

    public long getSDCardSizeMB() {
        return getSDCardSize() / (1024 * 1024);
    }

    public long getSDCardFreeMB() {
        return getSDCardFree() / (1024 * 1024);
    }

    public boolean isSDCardEmulated() {
        return SDCardEmulated;
    }

    public long getRAMSize() {
        return RAMSize;
    }

    public long getRAMSizeMB() {
        return getRAMSize() / (1024 * 1024);
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void send() {
        send(false);
    }

    public void send(final boolean showNotifications) {
        SuperSingleton superSingleton = SuperSingleton.getInstance(mContext);
        RequestQueue queue = superSingleton.getVolleyRequestQueue();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Telemetry.class, new TelemetrySerializer())
                .create();
        final String json = gson.toJson(this);

        Response.Listener<String> onSuccess = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("SendTelemetry", "Telemetry has been sent, server responded with: " + response);
                if (showNotifications) {
                    Toast.makeText(mContext, mContext.getString(R.string.thank_you_for_sending_telemetry), Toast.LENGTH_LONG).show();
                }
            }
        };
        Response.ErrorListener onFailure = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String msg = error.getMessage();
                Log.e("SendTelemetry", "Failed to send telemetry: " + msg);
                error.printStackTrace();

                if (showNotifications)
                    Toast.makeText(mContext, mContext.getString(R.string.error_sending_telemetry), Toast.LENGTH_LONG).show();
            }
        };

        Request<String> request = new StringRequest(StringRequest.Method.POST, TelemetryURL, onSuccess, onFailure) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                return json.getBytes();
            }
        };

        queue.add(request);
    }
}
