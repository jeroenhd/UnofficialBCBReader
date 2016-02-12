package nl.jeroenhd.app.bcbreader.data;

/**
 * A class representing a single page
 */
public class Page {
    private String commentary;
    private int number;
    private double chapter;

    public Page(String commentary, int number, double chapter) {
        this.commentary = commentary;
        this.number = number;
        this.chapter = chapter;
    }

    public String getCommentary() {
        return commentary;
    }

    public void setCommentary(String commentary) {
        this.commentary = commentary;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public double getChapter() {
        return chapter;
    }

    public void setChapter(double chapter) {
        this.chapter = chapter;
    }
}
