package nl.jeroenhd.app.bcbreader.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;

import nl.jeroenhd.app.bcbreader.data.databases.ChapterDatabase;

/**
 * A class representing a single page
 * Annotations are for DBFlow
 */
@SuppressWarnings("unused")
@Table(database = ChapterDatabase.class)
public class Page extends BaseModel implements Parcelable {
    public static final int NORMAL_WIDTH = 800;
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Page createFromParcel(Parcel in) {
            return new Page(in);
        }

        public Page[] newArray(int size) {
            return new Page[size];
        }
    };
    @ForeignKey(saveForeignKeyModel = false)
    ForeignKeyContainer<Chapter> chapterForeignKeyContainer;
    @Column
    private String description;
    @Column
    private Double page;
    @Column
    private Double chapter;

    public Page(String description, Double page, double chapter) {
        this.description = description;
        this.page = page;
        this.chapter = chapter;
    }

    public Page(Parcel data) {
        this.description = data.readString();
        this.page = data.readDouble();
    }

    public void associateChapter(Chapter chapter) {
        chapterForeignKeyContainer = FlowManager.getContainerAdapter(Chapter.class).toForeignKeyContainer(chapter);
    }

    public Double getChapter() {
        return chapter;
    }

    public void setChapter(Double chapter) {
        this.chapter = chapter;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPage() {
        return page;
    }

    public void setPage(Double page) {
        this.page = page;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(description);
        dest.writeDouble(page);
    }
}
