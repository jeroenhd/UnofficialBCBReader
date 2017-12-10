package nl.jeroenhd.app.bcbreader.data.databases;

import android.util.Log;

import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Method;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

import java.util.List;

import nl.jeroenhd.app.bcbreader.data.App;
import nl.jeroenhd.app.bcbreader.data.Chapter;
import nl.jeroenhd.app.bcbreader.data.Chapter_Table;

/**
 * CurrentChapter Database for DBFlow
 */
@Database(name = ChapterDatabase.NAME, version = ChapterDatabase.VERSION)
public class ChapterDatabase {
    static final String NAME = "ChapterInfo";
    static final int VERSION = 1;

    public static void SaveUpdate(List<Chapter> chapters) {
        FlowManager.getDatabase(ChapterDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<Chapter>() {
                            @Override
                            public void processModel(Chapter model) {
                                model.async().update();
                                model.async().save();
                            }
                        }).addAll(chapters).build())
                .error(new Transaction.Error() {
                    @Override
                    public void onError(Transaction transaction, Throwable error) {
                        Log.e(App.TAG, "SaveChapters: Error during transaction " + transaction.name());
                        error.printStackTrace();
                    }
                }).success(new Transaction.Success() {
            @Override
            public void onSuccess(Transaction transaction) {
                // Yay
                Log.d(App.TAG, "SaveChapters: Saved chapters!");
            }
        })
                .build().execute();
    }

    public static Chapter getLastChapter() {
        return new Select(Method.ALL_PROPERTY, Method.max(Chapter_Table.number)).from(Chapter.class).querySingle();
    }
}
