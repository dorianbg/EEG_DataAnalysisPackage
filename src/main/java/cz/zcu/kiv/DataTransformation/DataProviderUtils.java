package cz.zcu.kiv.DataTransformation;

import java.util.List;

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
 * DataProviderUtils, 2017/05/25 22:05 Dorian Beganovic
 *
 **********************************************************************************************************************/
public class DataProviderUtils {
    public static void writeEpochsToCSV(List<double[][]> epochs) throws Exception {

        //create a File class object and give the file the name employees.csv
        java.io.File file = new java.io.File("Epochs.csv");

        //Create a Printwriter text output stream and link it to the CSV File
        java.io.PrintWriter outfile = new java.io.PrintWriter(file);

        //Iterate the elements actually being used
        for (double[][] epoch : epochs) {
            for (int i = 0; i < epoch[2].length; i++) {
                outfile.write(epoch[2][i] + ",");
            }
            outfile.write("\n");
        }

        outfile.close();
    }

    public static float[] toFloatArray(double[] arr) {
        if (arr == null) {
            return null;
        }
        int n = arr.length;
        float[] ret = new float[n];
        for (int i = 0; i < n; i++) {
            ret[i] = (float) arr[i];
        }
        return ret;
    }

}
