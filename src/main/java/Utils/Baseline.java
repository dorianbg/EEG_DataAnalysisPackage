package Utils;

/**
 * @author Dorian Beganovic
 */
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
