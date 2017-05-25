import DataTransformation.DataProviderUtils;
import DataTransformation.OffLineDataProvider;

import org.junit.Test;

import java.util.List;

/**
 * Created by Dorian Beganovic on 16/05/2017.
 */
public class OfflineDataProviderTest {
    @Test
    public void testLoadingInfoTxtFile(){
        try {
            String[] files = {"/user/digitalAssistanceSystem/data/numbers/infoTrain.txt"};
            OffLineDataProvider odp =
                    new OffLineDataProvider(files);
            odp.loadData();
            List<double[][]> epochs = odp.getTrainingData();
            List<Double> targets = odp.getTrainingDataLabels();
            System.out.println("loaded " + epochs.size() + " epochs, each with size " + epochs.get(0).length + "x" + epochs.get(0)[0].length );
            System.out.println("loaded " + targets.size() + " labels");

            assert epochs.size() == 527;
            assert epochs.get(0).length == 3;
            assert epochs.get(0)[0].length == 750;

            DataProviderUtils.writeEpochsToCSV(epochs);

            double sum = 0;
            for (double[][] epoch : epochs){
                for (int i = 0; i < epoch.length; i++){
                    for (int j = 0; j < epoch[i].length; j++){
                        sum += epoch[i][j];
                    }
                }
            }
            System.out.println("Sum of epochs is" + sum);

            int targetsSum = 0;
            for (double target : targets){
                targetsSum += target;
            }
            System.out.println("Sum of targets is" + targetsSum);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLoadingFile1(){
        try {
            String[] files = {"data/numbers/Stankov/Stankov_26_1_20145_27.eeg","4"};
            OffLineDataProvider odp =
                    new OffLineDataProvider(files);
            odp.loadData();
            List<double[][]> epochs = odp.getTrainingData();
            List<Double> targets = odp.getTrainingDataLabels();
            System.out.println("loaded " + epochs.size() + " epochs, each with size " + epochs.get(0).length + "x" + epochs.get(0)[0].length );
            System.out.println("loaded " + targets.size() + " labels");
            assert epochs.size() == 44;
            assert epochs.get(0).length == 3;
            assert epochs.get(0)[0].length == 750;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
