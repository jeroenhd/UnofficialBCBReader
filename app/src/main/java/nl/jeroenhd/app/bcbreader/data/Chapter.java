package nl.jeroenhd.app.bcbreader.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.ArrayList;
import java.util.List;

import nl.jeroenhd.app.bcbreader.data.databases.ChapterDatabase;

/**
 * A class containing all data about a chapter
 * Annotations are for DBFlow
 */
@SuppressWarnings("unused")
@ModelContainer
@Table(database = ChapterDatabase.class)
public class Chapter extends BaseModel implements Parcelable {
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Chapter createFromParcel(Parcel in) {
            return new Chapter(in);
        }

        public Chapter[] newArray(int size) {
            return new Chapter[size];
        }
    };
    @Column
    @Expose
    @PrimaryKey
    Double number;

    @Column
    @Expose
    String title;

    @Column
    @Expose
    String description;

    @Column
    @Expose
    Integer pageCount;

    @Column
    @Expose
    Integer totalPages;

    @Column
    @Expose
    String yearPublished;

    @Column
    boolean favourite;

    @Expose
    List<Page> pageDescriptions;

    /**
     * Required for DBFlow
     */
    public Chapter() {

    }

    public Chapter(String title, String description, Integer pageCount, Integer totalPages, String yearPublished, Double number) {
        this.title = title;
        this.description = description;
        this.pageCount = pageCount;
        this.totalPages = totalPages;
        this.yearPublished = yearPublished;
        this.number = number;
    }

    /**
     * Make the chapter parcelable!!!
     */
    public Chapter(Parcel data) {
        this.title = data.readString();
        this.description = data.readString();
        this.pageCount = data.readInt();
        this.totalPages = data.readInt();
        this.yearPublished = data.readString();
        this.number = data.readDouble();

        pageDescriptions = new ArrayList<>();
        // this only works if Page is parcelable!
        data.readList(this.pageDescriptions, Page.class.getClassLoader());
        for (Page page : pageDescriptions) {
            page.setChapter(this.getNumber());
        }

        this.favourite = data.readInt() == 1;
    }

    public Double getNumber() {
        return number;
    }

    public void setNumber(Double number) {
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public String getYearPublished() {
        return yearPublished;
    }

    public void setYearPublished(String yearPublished) {
        this.yearPublished = yearPublished;
    }

    @OneToMany(methods = {OneToMany.Method.ALL}, variableName = "pageDescriptions")
    public List<Page> getPageDescriptions() {
        if (pageDescriptions == null)
        {
            pageDescriptions = SQLite.select()
                    .from(Page.class)
                    .where(Page_Table.chapter.eq(this.getNumber()))
                    .queryList();
        }
        return pageDescriptions;
    }

    public void setPageDescriptions(List<Page> pageDescriptions) {
        this.pageDescriptions = pageDescriptions;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

    @Override
    public int describeContents() {
        return 0;
    }


    /**
     * Get the chapter following this chapter. If there is no chapter, return null
     * @return The next chapter or null
     */
    @Nullable
    public Chapter getNext()
    {
        return new Select()
                .from(Chapter.class)
                .where(
                        Chapter_Table.number
                                .greaterThan(this.getNumber()))
                .limit(1)
                .orderBy(Chapter_Table.number, true)
                .querySingle();
    }

    /**
     * Get the chapter before this chapter. If there is no chapter, return null
     * @return The previous chapter or null
     */
    @Nullable
    public Chapter getPrevious()
    {
        return new Select()
                .from(Chapter.class)
                .where(
                        Chapter_Table.number
                                .lessThan(this.getNumber()))
                .limit(1)
                .orderBy(Chapter_Table.number, false)
                .querySingle();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.description);
        dest.writeInt(this.pageCount);
        dest.writeInt(this.totalPages);
        dest.writeString(this.yearPublished);
        dest.writeDouble(this.number);
        dest.writeList(pageDescriptions);
        dest.writeInt(favourite ? 1 : 0);
    }
}
