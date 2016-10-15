package nl.jeroenhd.app.bcbreader.test;

import junit.framework.Assert;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import nl.jeroenhd.app.bcbreader.data.check.UpdateTimes;

/**
 * A test to make sure the UpdateTimes math is solid
 */

public class UpdateTimesTest {
    @Test
    public void JustUpdated() throws Exception {
        UpdateTimes updateTimes = new UpdateTimes("N/A", "N/A", "1,2,3,4,5,6,7", "13");
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(Calendar.DAY_OF_WEEK, 3);
        calendar.set(Calendar.HOUR_OF_DAY, 13);

        Date checkDate = calendar.getTime();

        calendar.set(Calendar.HOUR_OF_DAY, 13);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date correctDate = calendar.getTime();

        Assert.assertEquals(correctDate, updateTimes.lastUpdate(checkDate));
    }

    @Test
    public void UpdatedYesterday() throws Exception {
        UpdateTimes updateTimes = new UpdateTimes("N/A", "N/A", "1,2,4,5,6,7", "13");
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(Calendar.DAY_OF_WEEK, 3);
        calendar.set(Calendar.HOUR_OF_DAY, 13);

        Date checkDate = calendar.getTime();
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.DAY_OF_WEEK, 2);
        Date correctDate = calendar.getTime();

        Assert.assertEquals(correctDate, updateTimes.lastUpdate(checkDate));
    }

    @Test
    public void AcrossWeekend() throws Exception {
        UpdateTimes updateTimes = new UpdateTimes("N/A", "N/A", "2,4,6", "13");
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(Calendar.DAY_OF_WEEK, 2);
        calendar.set(Calendar.HOUR_OF_DAY, 12);

        Date checkDate = calendar.getTime();

        calendar.set(Calendar.HOUR_OF_DAY, 13);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, -3);
        Date correctDate = calendar.getTime();

        Assert.assertEquals(correctDate, updateTimes.lastUpdate(checkDate));
    }

    @Test
    public void UpdateTodayButNotYet() throws Exception {
        UpdateTimes updateTimes = new UpdateTimes("N/A", "N/A", "1,2,3,4,5,6,7", "13");
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(Calendar.DAY_OF_WEEK, 3);
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        Date checkDate = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 13);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date correctDate = calendar.getTime();

        Assert.assertEquals(correctDate, updateTimes.lastUpdate(checkDate));
    }
}
