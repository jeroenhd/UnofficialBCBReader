package nl.jeroenhd.app.bcbreader.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * A class containing all data about a chapter
 */
public class Chapter implements Parcelable{
    String title, description;
    Integer pageCount, totalPages;
    String yearPublished;
    Double number;
    List<Page> pageDescriptions;

    public Chapter(String title, String description, Integer pageCount, Integer totalPages, String yearPublished, Double number) {
        this.title = title;
        this.description = description;
        this.pageCount = pageCount;
        this.totalPages = totalPages;
        this.yearPublished = yearPublished;
        this.number = number;
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

    /**
     * Make the chapter parcelable!!!
     */
    public Chapter(Parcel data)
    {
        this.title = data.readString();
        this.description = data.readString();
        this.pageCount = data.readInt();
        this.totalPages = data.readInt();
        this.yearPublished = data.readString();
        this.number = data.readDouble();

        pageDescriptions = new ArrayList<>();
        // this only works if Page is parcelable!
        data.readList(this.pageDescriptions, Page.class.getClassLoader());
    }

    public List<Page> getPageDescriptions() {
        return pageDescriptions;
    }

    public void setPageDescriptions(List<Page> pageDescriptions) {
        this.pageDescriptions = pageDescriptions;
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
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Chapter createFromParcel(Parcel in) {
            return new Chapter(in);
        }

        public Chapter[] newArray(int size) {
            return new Chapter[size];
        }
    };
}
