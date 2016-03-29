package nl.jeroenhd.app.bcbreader.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.ArrayList;
import java.util.List;

import nl.jeroenhd.app.bcbreader.data.databases.ChapterDatabase;

/**
 * A class containing all data about a chapter
 * Annotations are for DBFlow
 */
@SuppressWarnings("unused")
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


    @Override
    public void save() {
        super.save();
        for (Page page : this.pageDescriptions)
            page.save();
    }

    @PrimaryKey
    Double number;

    @Column
    String title;

    @Column
    String description;

    @Column
    Integer pageCount;

    @Column
    Integer totalPages;

    @Column
    String yearPublished;

    @Column
    boolean favourite;

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

    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "pageDescriptions")
    public List<Page> getPageDescriptions() {
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
