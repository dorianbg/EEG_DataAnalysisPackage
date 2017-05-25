package EEGDataLoading;


public class ChannelInfo {

    protected String name;
    protected int number;
    protected float resolution;
    protected String units;

    public final String getName() {
        return name;
    }
    public final int getNumber() {
        return number;
    }

    public final double getResolution() {
        return resolution;
    }

    public final String getUnits() {
        return units;
    }

    public ChannelInfo(int number, String strData) {

        this.number = number;
        String[] arr = strData.split("[,]", -1);
        name = arr[0];
        resolution = Float.parseFloat(arr[2]);
        units = arr[3];
    }
}
