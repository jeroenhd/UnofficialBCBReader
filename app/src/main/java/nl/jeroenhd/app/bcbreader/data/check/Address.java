package nl.jeroenhd.app.bcbreader.data.check;

/**
 * Class for the API check
 * Why is it called address? Only suitcase knows
 */
public class Address {
    private Double latestChapter;
    private int latestPage;
    private String latestGalleryHash;

    public Address(Double latestChapter, int latestPage, String latestGalleryHash) {
        this.latestChapter = latestChapter;
        this.latestPage = latestPage;
        this.latestGalleryHash = latestGalleryHash;
    }

    public Double getLatestChapter() {
        return latestChapter;
    }

    public void setLatestChapter(Double latestChapter) {
        this.latestChapter = latestChapter;
    }

    public int getLatestPage() {
        return latestPage;
    }

    public void setLatestPage(int latestPage) {
        this.latestPage = latestPage;
    }

    public String getLatestGalleryHash() {
        return latestGalleryHash;
    }

    public void setLatestGalleryHash(String latestGalleryHash) {
        this.latestGalleryHash = latestGalleryHash;
    }
}
