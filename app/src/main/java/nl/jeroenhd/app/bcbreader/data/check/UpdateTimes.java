package nl.jeroenhd.app.bcbreader.data.check;

/**
 * Class for the JSON API check
 * Contains when the comic updates
 */
public class UpdateTimes {
    String updateString, updateStringError, updateDays, updateHour;

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
}
