import cz.zcu.kiv.Classification.LogisticRegressionClassifier;
import cz.zcu.kiv.Classification.NeuralNetworkClassifier;
import cz.zcu.kiv.Classification.RandomForestClassifier;
import cz.zcu.kiv.Classification.SVMClassifier;
import cz.zcu.kiv.DataTransformation.OffLineDataProvider;
import cz.zcu.kiv.FeatureExtraction.IFeatureExtraction;
import cz.zcu.kiv.FeatureExtraction.WaveletTransform;
import cz.zcu.kiv.Utils.ClassificationStatistics;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Random;

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
 * ClassifierTest, 2017/06/18 14:28 Dorian Beganovic
 *
 **********************************************************************************************************************/
public class ClassifierTest {

    private static Log logger = LogFactory.getLog(ClassifierTest.class);

    @Test
    public void test() {
        try {

            // @ input folder parameter
            String[] files = {"/user/digitalAssistanceSystem/data/numbers/infoTrain.txt"};
            OffLineDataProvider odp =
                    new OffLineDataProvider(files);
            odp.loadData();
            List<double[][]> rawEpochs = odp.getData();
            List<Double> rawTargets = odp.getDataLabels();

            // @ feature extraction parameter
            IFeatureExtraction fe = new WaveletTransform(8, 512, 175, 16);

            // @ classifier parameter
            LogisticRegressionClassifier logisticRegressionClassifier = new LogisticRegressionClassifier();

            // total data
            List<double[][]> data = odp.getData();
            List<Double> targets = odp.getDataLabels();

            // shuffle the data but use the same seed !
            long seed = 1;
            Collections.shuffle(data,new Random(seed));
            Collections.shuffle(targets,new Random(seed));

            // training data
            List<double[][]> trainEpochs = data.subList(0,(int)(data.size()*0.7));
            List<Double> trainTargets = targets.subList(0,(int)(targets.size()*0.7));

            // testing data
            List<double[][]> testEpochs = data.subList((int)(data.size()*0.7),data.size());
            List<Double> testTargets = targets.subList((int)(targets.size()*0.7),targets.size());

            // train
            logisticRegressionClassifier.train(trainEpochs, trainTargets, fe);

            //test
            ClassificationStatistics classificationStatistics = logisticRegressionClassifier.test(testEpochs,testTargets);

            // @ classifier statistics
            logger.info("Classification statistics\n" + classificationStatistics);
//            assert classificationStatistics.calcAccuracy()==0.6415094339622641;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*
    @Test
    public void test2() {
        try {
            String[] files = {"/user/digitalAssistanceSystem/data/numbers/infoTrain.txt"};
            OffLineDataProvider odp =
                    new OffLineDataProvider(files);
            odp.loadData();
            List<double[][]> rawEpochs = odp.getData();
            List<Double> rawTargets = odp.getDataLabels();
            IFeatureExtraction fe = new WaveletTransform(8, 512, 175, 16);
            SVMClassifier svmClassifier = new SVMClassifier();

            // total data
            List<double[][]> data = odp.getData();
            List<Double> targets = odp.getDataLabels();

            // shuffle the data but use the same seed !
            long seed = 1;
            Collections.shuffle(data,new Random(seed));
            Collections.shuffle(targets,new Random(seed));

            // training data
            List<double[][]> trainEpochs = data.subList(0,(int)(data.size()*0.7));
            List<Double> trainTargets = targets.subList(0,(int)(targets.size()*0.7));

            // testing data
            List<double[][]> testEpochs = data.subList((int)(data.size()*0.7),data.size());
            List<Double> testTargets = targets.subList((int)(targets.size()*0.7),targets.size());

            // train
            svmClassifier.train(trainEpochs, trainTargets, fe);

            //test
            ClassificationStatistics classificationStatistics = svmClassifier.test(testEpochs,testTargets);

            // analyze the results
            logger.info("Classification statistics\n" + classificationStatistics);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Test
    public void test3() {
        try {
            String[] files = {"/user/digitalAssistanceSystem/data/numbers/infoTrain.txt"};
            OffLineDataProvider odp =
                    new OffLineDataProvider(files);
            odp.loadData();
            List<double[][]> rawEpochs = odp.getData();
            List<Double> rawTargets = odp.getDataLabels();
            IFeatureExtraction fe = new WaveletTransform(8, 512, 175, 16);
            RandomForestClassifier randomForestClassifier = new RandomForestClassifier();

            // total data
            List<double[][]> data = odp.getData();
            List<Double> targets = odp.getDataLabels();

            // shuffle the data but use the same seed !
            long seed = 1;
            Collections.shuffle(data,new Random(seed));
            Collections.shuffle(targets,new Random(seed));

            // training data
            List<double[][]> trainEpochs = data.subList(0,(int)(data.size()*0.7));
            List<Double> trainTargets = targets.subList(0,(int)(targets.size()*0.7));

            // testing data
            List<double[][]> testEpochs = data.subList((int)(data.size()*0.7),data.size());
            List<Double> testTargets = targets.subList((int)(targets.size()*0.7),targets.size());

            // train
            randomForestClassifier.train(trainEpochs, trainTargets, fe);

            //test
            ClassificationStatistics classificationStatistics = randomForestClassifier.test(testEpochs,testTargets);

            // analyze the results
            logger.info("Classification statistics\n" + classificationStatistics);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void test4() {
        try {
            String[] files = {"/user/digitalAssistanceSystem/data/numbers/infoTrain.txt"};
            OffLineDataProvider odp =
                    new OffLineDataProvider(files);
            odp.loadData();
            List<double[][]> rawEpochs = odp.getData();
            List<Double> rawTargets = odp.getDataLabels();
            IFeatureExtraction fe = new WaveletTransform(8, 512, 175, 16);
            GradientBoostedTreesClassifier gradientBoostedTreesClassifier = new GradientBoostedTreesClassifier();

            // total data
            List<double[][]> data = odp.getData();
            List<Double> targets = odp.getDataLabels();

            // shuffle the data but use the same seed !
            long seed = 1;
            Collections.shuffle(data,new Random(seed));
            Collections.shuffle(targets,new Random(seed));

            // training data
            List<double[][]> trainEpochs = data.subList(0,(int)(data.size()*0.7));
            List<Double> trainTargets = targets.subList(0,(int)(targets.size()*0.7));

            // testing data
            List<double[][]> testEpochs = data.subList((int)(data.size()*0.7),data.size());
            List<Double> testTargets = targets.subList((int)(targets.size()*0.7),targets.size());

            // train
            gradientBoostedTreesClassifier.train(trainEpochs, trainTargets, fe);

            //test
            ClassificationStatistics classificationStatistics = gradientBoostedTreesClassifier.test(testEpochs,testTargets);

            // analyze the results
            logger.info("Classification statistics\n" + classificationStatistics);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */

}
