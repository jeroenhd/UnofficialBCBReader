package nl.jeroenhd.app.bcbreader.data;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;

import nl.jeroenhd.app.bcbreader.data.databases.BCIChapterDatabase;

/**
 * A BCI page
 */
@Table(database = BCIChapterDatabase.class)
public class BCIPage extends BaseModel{
    @ForeignKey
    ForeignKeyContainer<BCIChapter> chapterForeignKeyContainer;

    @Column
    @PrimaryKey
    private String URL;

    @Column
    private String ChapterTitle;

    String getChapterTitle() {
        return ChapterTitle;
    }

    void setChapterTitle(String chapterTitle) {
        ChapterTitle = chapterTitle;
    }

    BCIPage()
    {
        this.ChapterTitle = null;
        this.URL = null;
    }

    BCIPage(String chapterTitle, String URL) {
        this.ChapterTitle = chapterTitle;
        this.URL = URL;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public void associateChapter(BCIChapter chapter) {
        chapterForeignKeyContainer = FlowManager.getContainerAdapter(BCIChapter.class).toForeignKeyContainer(chapter);
    }
}
