package nl.jeroenhd.app.bcbreader.data;

import android.util.Log;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.provider.ContentUri;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import nl.jeroenhd.app.bcbreader.data.databases.BCIChapterDatabase;

/**
 * Represents a single BCI chapter
 */

@Table(database = BCIChapterDatabase.class)
public class BCIChapter extends BaseModel {

    @Column
    @PrimaryKey
    private String Title;
    @Column
    private Date PostDate;
    @Column
    private int TotalPages;

    List<BCIPage> Parts;
    @Column
    private String ThumbURL;

    BCIChapter()
    {
        // Only here because DBFlow wants it here
        this.Title = null;
        this.PostDate = null;
        this.TotalPages = 0;
        this.Parts = new ArrayList<>();
        this.ThumbURL = null;
    }

    private BCIChapter(String title, Date postDate, int totalPages, List<BCIPage> parts, String thumbUrl) {
        Title = title;
        PostDate = postDate;
        TotalPages = totalPages;
        Parts = parts;
        this.ThumbURL = thumbUrl;
    }

    public String getTitle() {
        return Title;
    }

    Date getPostDate() {
        return PostDate;
    }

    int getTotalPages() {
        return TotalPages;
    }

    @OneToMany(methods = {OneToMany.Method.ALL}, variableName = "Parts")
    List<BCIPage> getParts() {

        if (Parts == null)
        {
            Parts = SQLite.select()
                    .from(BCIPage.class)
                    .where(BCIPage_Table.ChapterTitle.eq(this.getTitle()))
                    .queryList();
        }
        return Parts;
    }

    String getThumbURL() {
        return ThumbURL;
    }

    public void setTitle(String title) {
        Title = title;
    }

    void setPostDate(Date postDate) {
        PostDate = postDate;
    }

    void setTotalPages(int totalPages) {
        TotalPages = totalPages;
    }

    public void setParts(List<BCIPage> parts) {
        Parts = parts;
    }

    void setThumbURL(String thumbURL) {
        ThumbURL = thumbURL;
    }

    /**
     * Parse the HTML and form all BCI chapters
     * @param html The HTML for the members page
     * @return A list of BCI chapters
     */
    public static List<BCIChapter> getBCIChapters(String html)
    {
        Document document = Jsoup.parse(html);
        Elements elements = document.select("#exclusivecontent > ul > li");

        ArrayList<BCIChapter> bciChapters = new ArrayList<>(elements.size());
        for(Element e : elements)
        {
            try {
                URL thumbURL = new URL("https://www.bittersweetcandybowl.com" + e.select("img").get(0).attr("src"));
                Element titleLink = e.select("a.comictitle").get(0);
                String title = titleLink.text();

                String postDateString = e.select("span.date").get(0).text();

                // postDateString has this format: [m/d/y], [p] ["pages"]
                Date chapterDate = new SimpleDateFormat("m/d/y", Locale.US).parse(postDateString.split(",")[0]);
                int totalPages = Integer.parseInt(postDateString.split(" ")[1]);

                Element parts = e.select(".parts").get(0);

                int partsAmount = parts == null ? 1 : parts.select("a").size();
                List<BCIPage> bciPages = new ArrayList<>(partsAmount);

                if (parts == null)
                {
                    // Only a single part
                    bciPages.add(new BCIPage(title, titleLink.attr("href")));
                } else {
                    int i = 0;
                    for (Element part : e.select("a"))
                    {
                        bciPages.add(new BCIPage(title, part.attr("href")));
                    }
                }

                bciChapters.add(new BCIChapter(
                        title, chapterDate, totalPages, bciPages, thumbURL.toString()

                ));
            } catch (MalformedURLException urlEx) {
                urlEx.printStackTrace();
                Log.e(App.TAG, "Invalid URL! Element details: " + e.html());
            } catch (ParseException dateParseEx) {
                dateParseEx.printStackTrace();
                Log.e(App.TAG, "Invalid date! Element HTML: " + e.html());
            }
        }

        return bciChapters;
    }
}