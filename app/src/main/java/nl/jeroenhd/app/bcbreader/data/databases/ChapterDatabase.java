package nl.jeroenhd.app.bcbreader.data.databases;

import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.raizlabs.android.dbflow.runtime.transaction.process.SaveModelTransaction;
import com.raizlabs.android.dbflow.sql.language.Method;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.List;

import nl.jeroenhd.app.bcbreader.data.Chapter;
import nl.jeroenhd.app.bcbreader.data.Chapter_Table;

/**
 * CurrentChapter Database for DBFlow
 */
@Database(name = ChapterDatabase.NAME, version = ChapterDatabase.VERSION)
public class ChapterDatabase {
    static final String NAME = "ChapterInfo";
    static final int VERSION = 1;
    private static Chapter lastChapter;

    public static void SaveUpdate(List<Chapter> chapters) {
        TransactionManager.getInstance().addTransaction(new SaveModelTransaction<>(ProcessModelInfo.withModels(chapters)));
    }

    public static Chapter getLastChapter() {

        return new Select(Method.ALL_PROPERTY, Method.max(Chapter_Table.number)).from(Chapter.class).querySingle();
    }
}
