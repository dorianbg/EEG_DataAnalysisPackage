import cz.zcu.kiv.DataTransformation.OffLineDataProvider;
import cz.zcu.kiv.FeatureExtraction.WaveletTransform;
import cz.zcu.kiv.Utils.SignalProcessing;
import cz.zcu.kiv.Utils.SparkInitializer;
import cz.zcu.kiv.eegdsp.common.ISignalProcessingResult;
import cz.zcu.kiv.eegdsp.common.ISignalProcessor;
import cz.zcu.kiv.eegdsp.main.SignalProcessingFactory;
import cz.zcu.kiv.eegdsp.wavelet.discrete.WaveletResultDiscrete;
import cz.zcu.kiv.eegdsp.wavelet.discrete.WaveletTransformationDiscrete;
import cz.zcu.kiv.eegdsp.wavelet.discrete.algorithm.wavelets.WaveletDWT;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
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
 * FeatureExtractionTest, 2017/06/11 16:00 Dorian Beganovic
 *
 **********************************************************************************************************************/
public class FeatureExtractionTest {

    /*
    we will do a wavelet transform with following specifications:
        - 512 ms interval starting 175 ms after the beginning of each epoch is used
        - 5-level DWT using Daubechies 8 mother wavelet is performed
        - based on the results, 16 approximation coefficients of level 5 were used from each EEG channel
        - finally, all three results of DVVT were concatenated to form a feature vector of dimension 48.
    */
    private static Function<double[][], double[]> mapFunc=new Function<double[][], double[]>() {
        public double[] call(double[][] epoch) {
            WaveletTransform waveletTransformer = new WaveletTransform(8, 512,175,16);
            return waveletTransformer.extractFeatures(epoch);
        }
    };

    @Test
    public void test(){
        try{
            String[] files = {"/user/digitalAssistanceSystem/data/numbers/infoTrain.txt"};
            OffLineDataProvider odp =
                    new OffLineDataProvider(files);
            odp.loadData();
            List<double[][]> rawEpochs = odp.getTrainingData();
            List<Double> rawTargets = odp.getTrainingDataLabels();
            JavaRDD<double[][]> epochs = SparkInitializer.getJavaSparkContext().parallelize(rawEpochs);
            JavaRDD<Double> targets = SparkInitializer.getJavaSparkContext().parallelize(rawTargets);

            // a naive and ugly collect
            List<double[]> features = epochs.map(mapFunc).collect();

            System.out.println("Dimensions of resulting epochs are: " + features.size() + " x " + features.get(0).length);

            //tests
            assert features.size() == 527;
            for (int i = 0; i < features.size(); i++){
                assert features.get(i).length == 48;
            }


            double allFeatures = 0;
            double epochSum = 0;

            for (double[] epoch : features){
                epochSum = 0;
                for (double epoc : epoch){
                    epochSum += epoc;
                }
                //System.out.println(epochSum);
                allFeatures += epochSum;
            }
            //System.out.println("Sum of all features" + allFeatures);
            assert epochSum == 4.477696312014916;
            assert allFeatures == -299.9964225116246;

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
