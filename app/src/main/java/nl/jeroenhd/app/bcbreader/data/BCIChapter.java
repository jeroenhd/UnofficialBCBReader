package nl.jeroenhd.app.bcbreader.data;

import android.util.Log;

import com.raizlabs.android.dbflow.annotation.Table;
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
    private String Title;
    private Date PostDate;
    private int TotalPages;
    private BCIPage[] Parts;
    private URL ThumbURL;

    public BCIChapter(String title, Date postDate, int totalPages, BCIPage[] parts, URL thumbUrl) {
        Title = title;
        PostDate = postDate;
        TotalPages = totalPages;
        Parts = parts;
        this.ThumbURL = thumbUrl;
    }

    public String getTitle() {
        return Title;
    }

    public Date getPostDate() {
        return PostDate;
    }

    public int getTotalPages() {
        return TotalPages;
    }

    public BCIPage[] getParts() {
        return Parts;
    }

    public URL getThumbURL() {
        return ThumbURL;
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
                BCIPage[] bciPages = new BCIPage[partsAmount];

                if (parts == null)
                {
                    // Only a single part
                    bciPages[0] = new BCIPage(titleLink.attr("href"));
                } else {
                    int i = 0;
                    for (Element part : e.select("a"))
                    {
                        bciPages[i++] = new BCIPage(part.attr("href"));
                    }
                }

                bciChapters.add(new BCIChapter(
                        title, chapterDate, totalPages, bciPages, thumbURL

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