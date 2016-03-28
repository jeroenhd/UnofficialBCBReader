package nl.jeroenhd.app.bcbreader.data.check;

/**
 * A class for GSON to serialize
 * basically, it checks https://www.bittersweetcandybowl.com/app/json/check
 */
public class Check {
    Address address;
    UpdateTimes updatetimes;

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public UpdateTimes getUpdatetimes() {
        return updatetimes;
    }

    public void setUpdatetimes(UpdateTimes updatetimes) {
        this.updatetimes = updatetimes;
    }
}
