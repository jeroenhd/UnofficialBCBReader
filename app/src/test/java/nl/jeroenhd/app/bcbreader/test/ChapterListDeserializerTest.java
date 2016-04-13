package nl.jeroenhd.app.bcbreader.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import nl.jeroenhd.app.bcbreader.data.Chapter;
import nl.jeroenhd.app.bcbreader.data.ChapterListDeserializer;
import nl.jeroenhd.app.bcbreader.data.Page;

import static junit.framework.Assert.assertEquals;

/**
 * Test to check if the deserializer works
 */
public class ChapterListDeserializerTest {

    @Test
    public void TestDeserializer() throws Exception {
        TestBasicChapterList();

        TestChapterFile("src/test/assets/fullChapterList.json");
    }

    void TestBasicChapterList() throws FileNotFoundException {
        /*** Small chapter list test ***/
        // Test a small sample
        List<Chapter> singleChapterList = decodeString(loadFile("src/test/assets/singleChapterList.json"));
        List<Page> singleChapterPages = singleChapterList.get(0).getPageDescriptions();

        Assert.assertEquals(singleChapterList.size(), 1);
        Assert.assertEquals(singleChapterPages.size(), 2);
    }

    void TestChapterFile(String path) throws FileNotFoundException {
        /*** FULL chapter list test ***/
        // Test a full chapters file
        String largeInput = loadFile(path);
        List<Chapter> largeChapterList = decodeString(largeInput);
        Assert.assertEquals(114, largeChapterList.size());

        // Check for each chapter
        for (Chapter c : largeChapterList) {
            // * Whether or not the pagecount is correct
            assertEquals((int)c.getPageCount(), c.getPageDescriptions().size());

            for(Page p : c.getPageDescriptions())
            {
                // Whether or not all pages have the correct page number
                assertEquals(c.getNumber(), p.getChapter());
            }
        }
    }

    /**
     * Decode a chapter list JSON to a chapter list
     * @param jsonIn The JSON input (MUST be valid!)
     * @return The decoded chapter list
     */
    public List decodeString(String jsonIn)
    {
        GsonBuilder builder = new GsonBuilder();
        List chapterList = new ArrayList<>();

        // Have Gson use out ChapterListDeserializer for the chapter list
        builder.registerTypeAdapter(chapterList.getClass(), new ChapterListDeserializer());

        // Create the usable Gson object
        Gson gson = builder.create();

        return gson.fromJson(jsonIn, chapterList.getClass());
    }

    private String loadFile(String path) throws FileNotFoundException {
        FileInputStream f = new FileInputStream(path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(f));
        StringBuilder builder = new StringBuilder();
        String line;

        try {
            while (null != (line = reader.readLine()))
            {
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }
}