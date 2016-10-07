package nl.jeroenhd.app.bcbreader.data.check;

import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Class for the JSON API check
 * Contains when the comic updates
 */
public class UpdateTimes {
    private String updateString;
    private String updateStringError;
    private String updateDays;
    private String updateHour;

    public UpdateTimes(String updateString, String updateStringError, String updateDays, String updateHour) {
        this.updateString = updateString;
        this.updateStringError = updateStringError;
        this.updateDays = updateDays;
        this.updateHour = updateHour;
    }

    public String getUpdateString() {
        return updateString;
    }

    public void setUpdateString(String updateString) {
        this.updateString = updateString;
    }

    public String getUpdateStringError() {
        return updateStringError;
    }

    public void setUpdateStringError(String updateStringError) {
        this.updateStringError = updateStringError;
    }

    public String getUpdateDays() {
        return updateDays;
    }

    public void setUpdateDays(String updateDays) {
        this.updateDays = updateDays;
    }

    public String getUpdateHour() {
        return updateHour;
    }

    public void setUpdateHour(String updateHour) {
        this.updateHour = updateHour;
    }

    /**
     * Get the last update date and time
     *
     * @param currentDate The current date. You can find this using new Date()
     * @return The date and time of the last scheduled update
     */
    @Nullable
    public Date lastUpdate(Date currentDate) {
        // Get info about the current date
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(currentDate);
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);


        // Turn the string into an array of integers
        String[] updateDayStrings = updateDays.split(",");
        int[] updateDayNumbers = new int[updateDayStrings.length];
        for (int i = 0; i < updateDayStrings.length; i++) {
            updateDayNumbers[i] = Integer.parseInt(updateDayStrings[i]);
        }

        // Check for edge case: update day is today
        boolean updateDayToday = false;
        for (int updateDay : updateDayNumbers)
            if (updateDay == day)
                updateDayToday = true;

        // The update hour needs to have passed in order for the check to return anything meaningfull
        if (updateDayToday && hour >= Integer.parseInt(updateHour)) {
            //Today!
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(updateHour));
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            return calendar.getTime();
        }

        // This is simplified time math
        // This does not work with time zones etc.
        // So someone might miss a notification after moving timezones across the international date line
        // Should not be a big problem
        Calendar checkCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        checkCalendar.setTime(currentDate);
        checkCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(updateHour));
        checkCalendar.set(Calendar.MINUTE, 0);
        checkCalendar.set(Calendar.SECOND, 0);

        // Loop back till we find a correct day
        for (int daysWalkedback = 0; daysWalkedback < 7; daysWalkedback++) {
            // Back one day...
            checkCalendar.add(Calendar.DAY_OF_MONTH, -1);
            // Get the day of the week
            int checkDOW = checkCalendar.get(Calendar.DAY_OF_WEEK);

            // Get the date
            for (int checkDay : updateDayNumbers) {
                if (checkDOW == checkDay)
                    return checkCalendar.getTime();
            }
        }

        Log.e("BCBReader", "lastUpdate: invalid parameters! Walked back more than 7 days without finding an update day!");
        return null;
    }
}
