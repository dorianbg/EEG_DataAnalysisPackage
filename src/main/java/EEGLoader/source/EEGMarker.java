package EEGLoader.source;

public class EEGMarker {

    protected String name;
    protected String stimulus;
    protected int position;

    public EEGMarker clone() {
        EEGMarker varCopy = new EEGMarker();

        varCopy.name = this.name;
        varCopy.stimulus = this.stimulus;
        varCopy.position = this.position;

        return varCopy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStimulus() {
        return stimulus;
    }

    public void setStimulus(String stimulus) {
        this.stimulus = stimulus;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
