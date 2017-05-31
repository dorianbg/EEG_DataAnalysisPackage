package cz.zcu.kiv.Utils;

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
 * Baseline, 2017/05/25 22:05 Dorian Beganovic
 *
 **********************************************************************************************************************/
public class Baseline {

    public static void correct(float[] epoch, int prefix) {

        float baseline = 0;

        for (int i = 0; i < prefix; i++) {
            baseline += epoch[i];
        }

        baseline = baseline / prefix;

        for (int i = 0; i < epoch.length; i++) {
            epoch[i] -= baseline;
        }
    }

    public static void correct(double[] epoch, int prefix) {

        double baseline = 0;

        for (int i = 0; i < prefix; i++) {
            baseline += epoch[i];
        }

        baseline /= prefix;

        for (int i = 0; i < epoch.length; i++) {
            epoch[i] -= baseline;
        }
    }
}
