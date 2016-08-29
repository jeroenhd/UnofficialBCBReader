package nl.jeroenhd.app.bcbreader.data.check;

/**
 * A class for GSON to serialize
 * basically, it checks https://www.bittersweetcandybowl.com/app/json/check
 */
public class Check {
    private Address address;
    private UpdateTimes updatetimes;

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public UpdateTimes getUpdateTimes() {
        return updatetimes;
    }

    public void setUpdateTimes(UpdateTimes updatetimes) {
        this.updatetimes = updatetimes;
    }
}
