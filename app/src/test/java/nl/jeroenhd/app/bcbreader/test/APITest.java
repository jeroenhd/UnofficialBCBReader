package nl.jeroenhd.app.bcbreader.test;

import junit.framework.Assert;

import org.junit.Test;

import java.util.Map;

import nl.jeroenhd.app.bcbreader.data.API;

import static junit.framework.Assert.assertEquals;

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