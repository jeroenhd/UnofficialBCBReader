package nl.jeroenhd.app.bcbreader.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A class representing a single page
 */
@SuppressWarnings("unused")
public class Page implements Parcelable {
    public static final int NORMAL_WIDTH = 800;
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Page createFromParcel(Parcel in) {
            return new Page(in);
        }

        public Page[] newArray(int size) {
            return new Page[size];
        }
    };
    private String description;
    private Double page;
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
