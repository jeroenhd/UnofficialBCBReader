package nl.jeroenhd.app.bcbreader.data.databases;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Chapter Database for DBFlow
 */
@Database(name = ChapterDatabase.NAME, version = ChapterDatabase.VERSION)
public class ChapterDatabase {
    public static final String NAME = "ChapterInfo";
    public static final int VERSION = 1;
}
