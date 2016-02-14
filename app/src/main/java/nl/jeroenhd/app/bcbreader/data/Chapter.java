package nl.jeroenhd.app.bcbreader.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A class containing all data about a chapter
 */
public class Chapter implements Parcelable{
    private String title, description;
    private int pageCount, totalPages;
    private String yearPublished;
    private float number;

    public Chapter(String title, String description, int pageCount, int totalPages, String yearPublished, float number) {
        this.title = title;
        this.description = description;
        this.pageCount = pageCount;
        this.totalPages = totalPages;
        this.yearPublished = yearPublished;
        this.number = number;
    }

    public float getNumber() {
        return number;
    }

    public void setNumber(float number) {
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

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
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
        this.number = data.readFloat();
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
        dest.writeFloat(this.number);
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
