package nl.jeroenhd.app.bcbreader.data.databases;

import android.util.Log;

import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

import java.util.List;

import nl.jeroenhd.app.bcbreader.data.App;
import nl.jeroenhd.app.bcbreader.data.BCIChapter;
import nl.jeroenhd.app.bcbreader.data.Chapter;

/**
 * A DBFlow helper class to store the BCI chapters
 */
@Database(name = BCIChapterDatabase.NAME, version = BCIChapterDatabase.VERSION)
public class BCIChapterDatabase {

    public static final String NAME = "bcichapters";
    public static final int VERSION = 1;

    public static void SaveChapters(List<BCIChapter> chapters) {
        FlowManager.getDatabase(BCIChapterDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<BCIChapter>() {
                            @Override
                            public void processModel(BCIChapter model) {
                                model.update();
                                model.save();
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
                Log.d(App.TAG, "BCIChapterDatabase::SaveChapters: Saved chapters!");
            }
        })
                .build()
                .execute();
    }
}
