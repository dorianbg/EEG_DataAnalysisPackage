import cz.zcu.kiv.DataTransformation.DataProviderUtils;
import cz.zcu.kiv.DataTransformation.OffLineDataProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import java.io.PrintWriter;
import java.util.Arrays;
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
 * Baseline, 2017/05/25 22:05 Dorian Beganovic
 *
 **********************************************************************************************************************/
public class OfflineDataProviderTest {
    private static Log logger = LogFactory.getLog(OffLineDataProvider.class);

    @Test
    public void testLoadingInfoTxtFile(){
        try {
            String[] files = {"/user/digitalAssistanceSystem/data/numbers/infoTrain.txt"};
            OffLineDataProvider odp =
                    new OffLineDataProvider(files);
            odp.loadData();
            List<double[][]> epochs = odp.getTrainingData();
            List<Double> targets = odp.getTrainingDataLabels();
            logger.info("loaded " + epochs.size() + " epochs, each with size " + epochs.get(0).length + "x" + epochs.get(0)[0].length );
            logger.info("loaded " + targets.size() + " labels");

            assert epochs.size() == 527;
            assert epochs.get(0).length == 3;
            assert epochs.get(0)[0].length == 750;
            DataProviderUtils.writeEpochsToCSV(epochs);

            PrintWriter file = new PrintWriter("/Users/dorianbeganovic/Desktop/sparkFE.txt");
            double epochsSum = 0;
            for (double[][] epoch : epochs){
                for (int i = 0; i < epoch.length; i++){
                    double rowSum = 0;
                    for (int j = 0; j < epoch[i].length; j++){
                        rowSum += epoch[i][j];
                        //logger.info("Row epochsSum is" + rowSum);
                    }
                    file.println(rowSum);
                    //System.out.println(rowSum);
                    epochsSum += rowSum;
                }
            }
            logger.info("Sum of epochs is" + epochsSum);
            assert epochsSum == (-1.1528369024642944E7);

            int targetsSum = 0;
            for (double target : targets){
                targetsSum += target;
            }
            logger.info("Sum of targets is" + targetsSum);
            assert targetsSum==263;

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

            logger.info("loaded " + epochs.size() + " epochs, each with size " + epochs.get(0).length + "x" + epochs.get(0)[0].length );
            logger.info("loaded " + targets.size() + " labels");
            assert epochs.size() == 44;
            assert epochs.get(0).length == 3;
            assert epochs.get(0)[0].length == 750;


            double sum = 0;
            for (double[][] epoch : epochs){
                for (int i = 0; i < epoch.length; i++){
                    double rowSum = 0;
                    for (int j = 0; j < epoch[i].length; j++){
                        rowSum += epoch[i][j];
                        System.out.println(rowSum);
                        logger.info("Row sum is" + rowSum);
                    }
                    sum += rowSum;
                }
            }
            logger.info("Sum of epochs is" + sum);

            int targetsSum = 0;
            for (double target : targets){
                targetsSum += target;
            }
            logger.info("Sum of targets is" + targetsSum);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
