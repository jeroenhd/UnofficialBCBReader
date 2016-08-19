package nl.jeroenhd.app.bcbreader.test;

import junit.framework.Assert;

import org.junit.Test;

import java.util.Map;

import nl.jeroenhd.app.bcbreader.data.API;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Class for checking if the API works as intended
 */
public class APITest {

    @Test
    public void testRequestHeaders() throws Exception {
        Map<String, String> headers = API.RequestHeaders();
        Assert.assertTrue(headers.containsKey("User-Agent"));
    }

    @Test
    public void testGetQualitySuffix() throws Exception {
        //TODO: Find a way to fake a SharedPreferences object
    }

    @Test
    public void testIsJpegChapter() {
        double[] singleChapters = {16.1, 17.1, 22.1, 26.1, 35.0, 35.1, 38.1};
        double[][] chapterRanges = {{70, 88}};

        for (double singleChapter : singleChapters) {
            assertTrue(API.isJpegChapter(singleChapter, ""));
        }

        for (double[] range : chapterRanges) {
            double start = range[0];
            double end = range[0];
            for (double c = start; c < end; c++) {
                assertTrue(API.isJpegChapter(c, ""));
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testFormatPageUrl_deprecated() throws Exception {
        assertEquals("https://blasto.enterprises/comics/1/1@m.png", API.FormatPageUrl(1.0, 1.0));
        assertEquals("https://blasto.enterprises/comics/70/1@m.jpg", API.FormatPageUrl(70.0, 1.0));
        assertEquals("https://blasto.enterprises/comics/9.1/1@m.png", API.FormatPageUrl(9.1, 1.0));
    }

    @Test
    public void testFormatPageUrl() throws Exception {
        /// MOBILE

        // Basic
        assertEquals("https://blasto.enterprises/comics/1/1@m.png", API.FormatPageUrl(1.0, 1.0, "@m"));
        // JPEG URL
        assertEquals("https://blasto.enterprises/comics/70/1@m.jpg", API.FormatPageUrl(70.0, 1.0, "@m"));
        // Decimal in chapter number
        assertEquals("https://blasto.enterprises/comics/9.1/1@m.png", API.FormatPageUrl(9.1, 1.0, "@m"));

        /// DESKTOP
        // Basic
        assertEquals("https://blasto.enterprises/comics/1/1@2x.png", API.FormatPageUrl(1.0, 1.0, "@2x"));
        // JPEG URL
        assertEquals("https://blasto.enterprises/comics/70/1@2x.jpg", API.FormatPageUrl(70.0, 1.0, "@2x"));
        // Decimal in chapter number
        assertEquals("https://blasto.enterprises/comics/9.1/1@2x.png", API.FormatPageUrl(9.1, 1.0, "@2x"));

        /// RETINA
        // Basic
        assertEquals("https://blasto.enterprises/comics/1/1.png", API.FormatPageUrl(1.0, 1.0, ""));
        // JPEG URL
        assertEquals("https://blasto.enterprises/comics/70/1.jpg", API.FormatPageUrl(70.0, 1.0, ""));
        // Decimal in chapter number
        assertEquals("https://blasto.enterprises/comics/9.1/1.png", API.FormatPageUrl(9.1, 1.0, ""));
    }

    /**
     * Test if FormatPageLink does what it's supposed to do
     *
     * @throws Exception Happens if the test fails
     */
    @Test
    public void testFormatPageLink() throws Exception {
        // Basic
        assertEquals("http://bcb.cat/c1/p1/", API.FormatPageLink(1.0, 1));
        // JPEG URL
        assertEquals("http://bcb.cat/c70/p1/", API.FormatPageLink(70.0, 1));
        // Decimal in chapter number
        assertEquals("http://bcb.cat/c9.1/p1/", API.FormatPageLink(9.1, 1));
    }

    @Test
    public void testFormatLqThumbURL() throws Exception {
        // Basic
        assertEquals("https://blasto.enterprises/app/comics/lqthumb/1-1.jpg", API.FormatLqThumbURL(1.0, 1.0));
        // Special case for FormatPageURL (JPEG files)
        assertEquals("https://blasto.enterprises/app/comics/lqthumb/70-1.jpg", API.FormatLqThumbURL(70.0, 1.0));
        // Decimal in chapter number
        assertEquals("https://blasto.enterprises/app/comics/lqthumb/9.1-1.jpg", API.FormatLqThumbURL(9.1, 1.0));
    }

    @Test
    public void testFormatChapterThumbURL() throws Exception {
        // Basic
        assertEquals("https://blasto.enterprises/app/comics/icon/1.png", API.FormatChapterThumbURL(1.0));
        // Decimal in chapter number
        assertEquals("https://blasto.enterprises/app/comics/icon/9.1.png", API.FormatChapterThumbURL(9.1));
        // JPEG chapter
        assertEquals("https://blasto.enterprises/app/comics/icon/70.png", API.FormatChapterThumbURL(70.0));
        // Another test for no real reason
        assertEquals("https://blasto.enterprises/app/comics/icon/92.png", API.FormatChapterThumbURL(92.0));
    }
}