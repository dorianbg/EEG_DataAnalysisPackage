package cz.zcu.kiv.DataTransformation;/*
 * Created by Dorian Beganovic on 16/05/2017.
 */

import cz.zcu.kiv.Utils.Const;

import java.util.Arrays;
/***********************************************************************************************************************
 *
 * This file is part of the Spark_EEG_Analysis project

 * ==========================================
 *
 * Copyright (C) 2017 by University of West Bohemia (http://www.zcu.cz/en/)
 *
 ***********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 ***********************************************************************************************************************
 *
 * EpochHolder, 2017/05/25 22:05 Dorian Beganovic
 *
 **********************************************************************************************************************/
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
