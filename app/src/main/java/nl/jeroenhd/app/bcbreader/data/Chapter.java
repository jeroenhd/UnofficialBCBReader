package nl.jeroenhd.app.bcbreader.data;

/**
 * A class containing all data about a chapter
 */
public class Chapter {
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
}
