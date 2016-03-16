package nl.jeroenhd.app.bcbreader.data;

/**
 * A class representing a single page
 */
public class Page {
    private String description;
    private Double page;

    public Page(String description, Double page, double chapter) {
        this.description = description;
        this.page = page;
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
}
