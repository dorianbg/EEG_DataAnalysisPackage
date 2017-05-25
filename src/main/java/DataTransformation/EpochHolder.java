package DataTransformation;/*
 * Created by Dorian Beganovic on 16/05/2017.
 */

import Utils.Const;

import java.util.Arrays;
/**
 * Serves as a data container transfered to observers
 *
 * @author Lukas Vareka
 *
 */
class EpochHolder {

    /**
     * Channels [Fz, Cz, Pz] * time samples
     */
    private final double[][] epoch;
    private boolean isTarget = false;// klasifikator pracuje s polem typu double, ne float

    /**
     * Guessed number-related stimulus 1 - 9
     */
    private int stimulusIndex;

    public EpochHolder() {
        this.epoch = new double[Const.USED_CHANNELS][Const.POSTSTIMULUS_VALUES];
        this.stimulusIndex = -1;
    }

    public EpochHolder(double[][] epoch, int stimulusIndex) {
        this.epoch = epoch;
        this.stimulusIndex = stimulusIndex;
    }

    public double[][] getEpoch() {
        return epoch;
    }

    public int getStimulusIndex() {
        return stimulusIndex;
    }

    public boolean isTarget() {
        return isTarget;
    }

    public void setTarget(boolean isTarget) {
        this.isTarget = isTarget;
    }

    public void setStimulusIndex(int stimulusIndex) {
        this.stimulusIndex = stimulusIndex;
    }

    public void setFZ(float[] fz, int offset) {
        for (int i = 0; i < Const.POSTSTIMULUS_VALUES; i++) {
            epoch[0][i] = (double) fz[i + offset];
        }
    }

    public void setCZ(float[] cz, int offset) {
        for (int i = 0; i < Const.POSTSTIMULUS_VALUES; i++) {
            epoch[1][i] = (double) cz[i + offset];
        }
    }

    public void setPZ(float[] pz, int offset) {
        for (int i = 0; i < Const.POSTSTIMULUS_VALUES; i++) {
            epoch[2][i] = (double) pz[i + offset];
        }
    }

    @Override
    public String toString() {
        return "FZ: " + Arrays.toString(epoch[0]) + "\n"
                + "CZ: " + Arrays.toString(epoch[1]) + "\n"
                + "PZ: " + Arrays.toString(epoch[2]) + "\n\n";
    }
}
