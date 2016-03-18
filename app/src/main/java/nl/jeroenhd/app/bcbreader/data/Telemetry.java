package nl.jeroenhd.app.bcbreader.data;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import java.io.File;

/**
 * Class to detect telemetry
 */
public class Telemetry {
    String Model, AndroidVersion;
    long InternalSize, InternalFree;
    long SDCardSize, SDCardFree;
    boolean SDCardEmulated;

    protected static Telemetry instance;

    protected Telemetry() {
        Model = Build.MODEL;
        AndroidVersion = Build.VERSION.RELEASE;

        // External storage available (could ebe emulated)
        if (android.os.Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            SDCardEmulated = Environment.isExternalStorageEmulated();
            SDCardSize = getVolumeSizeByPath(Environment.getExternalStorageDirectory());
            SDCardFree = getVolumeFreeByPath(Environment.getExternalStorageDirectory());
        }
        InternalSize = getVolumeSizeByPath(Environment.getDataDirectory());
        InternalFree = getVolumeFreeByPath(Environment.getDataDirectory());
    }

    protected long getVolumeSizeByPath(File path)
    {
        StatFs stat = new StatFs(path.toString());
        long blockSize, blockCount;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = stat.getBlockSizeLong();
            blockCount = stat.getBlockCountLong();
        } else {
            blockSize = stat.getBlockSize();
            blockCount = stat.getBlockCount();
        }
        return blockCount*blockSize;
    }

    protected long getVolumeFreeByPath(File path)
    {
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

    public static Telemetry getInstance(Context context)
    {
        if (null == instance)
            instance = new Telemetry();

        return instance;
    }
}
