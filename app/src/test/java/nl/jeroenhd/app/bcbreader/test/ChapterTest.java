package nl.jeroenhd.app.bcbreader.test;

import android.os.Parcel;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import nl.jeroenhd.app.bcbreader.data.Chapter;
import nl.jeroenhd.app.bcbreader.data.Page;

import static org.mockito.Mockito.mock;

/**
 * Class to test chapters
 */

public class ChapterTest {

    @Test
    public void testParcelable() {
        Chapter source = new Chapter("Test chapter", "This is a test chapter", 10, 15, "Just now", 99.9);
        List<Page> pages = new ArrayList<>();
        for (int i = 0; i < source.getPageCount(); i++)
        {
            pages.add(new Page("Test page " + i, (double)i, source.getNumber()));
        }
        source.setPageDescriptions(pages);

        Parcel p = mock(Parcel.class);
        source.writeToParcel(p, 0);
        p.setDataPosition(0);
        Chapter copy = (Chapter) Chapter.CREATOR.createFromParcel(p);

        // Uncomment as soon as the SDK supports parcelables
        //assertEquals(source, copy);
    }
}
