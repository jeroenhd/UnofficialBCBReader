package nl.jeroenhd.app.bcbreader.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nl.jeroenhd.app.bcbreader.data.Chapter;
import nl.jeroenhd.app.bcbreader.data.ChapterListDeserializer;
import nl.jeroenhd.app.bcbreader.data.Page;

import static junit.framework.Assert.assertEquals;

/**
 * Test to check if the deserializer works
 */
public class ChapterListDeserializerTest {

    @Test
    public void TestBasicChapterList() throws FileNotFoundException {
        /*** Small chapter list test ***/
        // Test a small sample
        List<Chapter> singleChapterList = decodeStringBeta(loadFile("src/test/assets/singleChapterList.json"));
        List<Page> singleChapterPages = singleChapterList.get(0).getPageDescriptions();

        Assert.assertEquals(singleChapterList.size(), 1);
        Assert.assertEquals(singleChapterPages.size(), 2);
    }

    @Test
    public void TestDeserializerFiles() throws Exception {
        TestChapterFile("src/test/assets/fullChapterList.json");
    }

    @Test
    public void TestSingleChapterParse() {
        String chapter = "{\n" +
                "\t\"title\": \"Example chapter\",\n" +
                "\t\"description\": \"This is an example\",\n" +
                "\t\"pageCount\": 3,\n" +
                "\t\"totalPages\": 3,\n" +
                "\t\"yearPublished\": \"Testing system\",\n" +
                "\t\"pageDescriptions\": [{\n" +
                "\t\t\"page\": 1,\n" +
                "\t\t\"description\": \"<p>Test 1</p>\"\n" +
                "\t}, {\n" +
                "\t\t\"page\": 2,\n" +
                "\t\t\"description\": \"<p>Test 2</p>\"\n" +
                "\t}, {\n" +
                "\t\t\"page\": 3,\n" +
                "\t\t\"description\": \"<p>Test 3</p>\"\n" +
                "\t}]\n" +
                "}";

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Chapter c = gson.fromJson(chapter, Chapter.class);


        Assert.assertEquals("Example chapter", c.getTitle());
        Assert.assertEquals("This is an example", c.getDescription());
        Assert.assertEquals("3", c.getPageCount().toString());
        Assert.assertEquals("3", c.getTotalPages().toString());
        Assert.assertEquals("Testing system", c.getYearPublished());
        Assert.assertEquals(3, c.getPageDescriptions().size());
    }

    private void TestChapterFile(String path) throws FileNotFoundException {
        /*** FULL chapter list test ***/
        // Test a full chapters file
        String largeInput = loadFile(path);
        List<Chapter> largeChapterList = decodeStringBeta(largeInput);/*decodeString(largeInput);*/
        Assert.assertEquals(114, largeChapterList.size());

        // Check for each chapter
        for (Chapter c : largeChapterList) {
            // * Whether or not the page count is correct
            assertEquals((int) c.getPageCount(), c.getPageDescriptions().size());

            for (Page p : c.getPageDescriptions()) {
                // Whether or not all pages have the correct page number
                assertEquals(c.getNumber(), p.getChapter());
            }
        }
    }

    /**
     * Decode a chapter list JSON to a chapter list
     *
     * @param jsonIn The JSON input (MUST be valid!)
     * @return The decoded chapter list
     */
    @Deprecated
    private List<Chapter> decodeString(String jsonIn) {
        GsonBuilder builder = new GsonBuilder();
        List<Chapter> chapterList = new ArrayList<>();

        // Have Gson use out ChapterListDeserializer for the chapter list
        builder.registerTypeAdapter(chapterList.getClass(), new ChapterListDeserializer());

        // Create the usable Gson object
        Gson gson = builder.create();

        chapterList = gson.fromJson(jsonIn, chapterList.getClass());

        return chapterList;
    }

    private List<Chapter> decodeStringBeta(String jsonIn) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        List<Chapter> chapterList = new ArrayList<>();

        Type mapType = new TypeToken<Map<String, Chapter>>() {
        }.getType();
        Map<String, Chapter> chapterMap = gson.fromJson(jsonIn, mapType);

        for (String key : chapterMap.keySet()) {
            Chapter c = chapterMap.get(key);
            c.setNumber(Double.parseDouble(key));
            chapterList.add(c);

            for (Page p : c.getPageDescriptions()) {
                p.setChapter(c.getNumber());
            }
        }

        return chapterList;
    }

    private String loadFile(String path) throws FileNotFoundException {
        FileInputStream f = new FileInputStream(path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(f));
        StringBuilder builder = new StringBuilder();
        String line;

        try {
            while (null != (line = reader.readLine())) {
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }
}