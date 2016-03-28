package nl.jeroenhd.app.bcbreader.data.check;

/**
 * Class for the API check
 * Why is it called address? Only suitcase knows
 */
public class Address {
    Double latestChapter, latestPage;
    String latestGalleryHash;

    public Address(Double latestChapter, Double latestPage, String latestGalleryHash) {
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

    public Double getLatestPage() {
        return latestPage;
    }

    public void setLatestPage(Double latestPage) {
        this.latestPage = latestPage;
    }

    public String getLatestGalleryHash() {
        return latestGalleryHash;
    }

    public void setLatestGalleryHash(String latestGalleryHash) {
        this.latestGalleryHash = latestGalleryHash;
    }
}
