package OffLineDataProvider;

import java.util.List;

/**
 * @author Dorian Beganovic
 */
public class Utils {
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
