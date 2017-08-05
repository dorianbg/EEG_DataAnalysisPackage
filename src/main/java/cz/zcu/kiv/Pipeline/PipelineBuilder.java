package cz.zcu.kiv.Pipeline;

import cz.zcu.kiv.Classification.*;
import cz.zcu.kiv.DataTransformation.OffLineDataProvider;
import cz.zcu.kiv.FeatureExtraction.IFeatureExtraction;
import cz.zcu.kiv.FeatureExtraction.WaveletTransform;
import cz.zcu.kiv.Utils.ClassificationStatistics;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

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
 * PipelineBuilder, 2017/07/11 22:54 Dorian Beganovic
 *
 **********************************************************************************************************************/
public class PipelineBuilder {

    private static Log logger = LogFactory.getLog(PipelineBuilder.class);
    private String query;

    public PipelineBuilder(String query){
        this.query = query;

    }

    private Map<String, String> getQueryMap(String query)
    {
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<String, String>();
        for (String param : params)
        {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
        }
        return map;
    }

    /**
     * The expected query parameters are:

     1. Input file
     - a) info_file={*path_to_file}
     OR
     - eeg_file={*path_to_file} AND guessed_num={*number}
     (REQUIRED)

     2. Feature extraction
     - fe = {dwt-8} (REQUIRED)

     3. Classification
     - a) train_clf = {svm,xgboost,logreg,rf}
     OR
     load_clf={svm,xgboost,logreg,rf} AND load_name={src/main/resources/Classifiers/(*name*) }
     (REQUIRED)
     - b) save_clf={true,false} AND save_name={*name}   (OPTIONAL)

     *
     * The method returns a JSON object containing the classifier statistics
     * as well as an array of predicted vs. actual values
     * @throws Exception
     */
    public void execute() throws Exception {
        Map<String,String> queryMap = getQueryMap(query);
        System.setProperty("logfilename", System.getProperty("user.home") + "/spark_server/logs/" + queryMap.get("id") );
        logger.info(queryMap);
        String[] files = new String[5];

        /*
           1. get the input
           a) within a info.txt file
           b) a .eeg file reference and the target number
         */
        if (queryMap.containsKey("info_file")){
            files[0]=queryMap.get("info_file");
        }
        else if (queryMap.containsKey("eeg_file") && queryMap.containsKey("guessed_num") ){
            files[0] = queryMap.get("eeg_file");
            files[1] = queryMap.get("guessed_num");
        }
        else {
            throw new IllegalArgumentException("Missing the input file argument");
        }

        OffLineDataProvider odp =
                new OffLineDataProvider(files);
        odp.loadData();
        List<double[][]> rawEpochs = odp.getData();
        List<Double> rawTargets = odp.getDataLabels();

        /*
            2. set the feature extraction
         */
        // @ feature extraction parameter

        IFeatureExtraction fe;
        if (queryMap.containsKey("fe")){
            String type = queryMap.get("fe");
            if(type.equals("dwt-8")){
                fe =  new WaveletTransform(8, 512, 175, 16);
            }
            else{
                throw new IllegalArgumentException("Unsupported feature extraction argument");
            }
        }
        else{
            throw new IllegalArgumentException("Missing the feature extraction argument");
        }

        /*
            3. set the classifier, either:
                a) load a classifier
                b) train a new one
            also there is the option to save a classifier
         */
        // @ classifier parameter
        IClassifier classifier;
        ClassificationStatistics classificationStatistics;

        if(queryMap.containsKey("train_clf")){

            String classifierType = queryMap.get("train_clf");
            logger.info("Training a classifier of type " + classifierType);

            switch (classifierType){
                case "svm": classifier = new SVMClassifier();
                    break;
                case "logreg": classifier = new LogisticRegressionClassifier();
                    break;
                case "dt": classifier = new DecisionTreeClassifier();
                    break;
                case "rf" : classifier = new RandomForestClassifier();
                    break;
                default: classifier = null;
                    throw new IllegalArgumentException("Unsupported classifier argument");
            }
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

            // Load config
            HashMap<String,String> config = new HashMap<>(10);
            for (String key : queryMap.keySet()){
                if(key.startsWith("config_")){
                    config.put(key,queryMap.get(key));
                }
            }

            logger.info("Loaded the data into memory");
            // into the classifier
            classifier.setConfig(config);

            logger.info("Set the classifier config");

            // train
            classifier.train(trainEpochs, trainTargets, fe);

            logger.info("Trained the model");

            if(queryMap.containsKey("save_clf")){
                if(queryMap.get("save_clf").equals("true")){
                    logger.info("Saving classifier");
                    if(queryMap.containsKey("save_name")){
                        classifier.save(System.getProperty("user.home") + "/spark_server/classifiers/" + queryMap.get("save_name"));
                    }
                    else{
                        throw new IllegalArgumentException("Please provide a location to save a classifier within the  save_location query parameter");
                    }
                }
            }
            logger.info("Saved classifier");

            //test
            classificationStatistics = classifier.test(testEpochs,testTargets);

            logger.info("Got the classification statistics");

        }
        else if(queryMap.containsKey("load_clf")){

            logger.info("Loading a saved classifier");

            // get the type of classifier
            String classifierType = queryMap.get("load_clf");
            // get the location of the classifier
            String classifierPath;
            if(queryMap.containsKey("load_name")){
                classifierPath = queryMap.get("load_name");
            }
            else{
                throw new IllegalArgumentException("Classifier location not provided");
            }

            switch (classifierType){
                case "svm": classifier = new SVMClassifier();
                    break;
                case "logreg": classifier = new LogisticRegressionClassifier();
                    break;
                case "dt": classifier = new DecisionTreeClassifier();
                    break;
                case "rf" : classifier = new RandomForestClassifier();
                    break;
                default: classifier = null;
                    throw new IllegalArgumentException("Unsupported classifier argument");
            }
            logger.info("Classifier type is " + classifierType);
            logger.info("Classifier path is " + classifierPath);


            // total data
            List<double[][]> data = odp.getData();
            List<Double> targets = odp.getDataLabels();

            // shuffle the data but use the same seed !
            long seed = 1;
            Collections.shuffle(data,new Random(seed));
            Collections.shuffle(targets,new Random(seed));
            logger.info("Loaded the data into memory");

            // train
            classifier.setFeatureExtraction(fe);
            classifier.load(System.getProperty("user.home") + "/spark_server/classifiers/" + classifierPath);

            logger.info("Loaded the classifier classifier");

            //test
            classificationStatistics = classifier.test(data,targets);
            logger.info("Produced the classification statistics");

        }
        else{
            throw new IllegalArgumentException("Missing classifier argument");
        }



        System.out.println(classificationStatistics);
        if(queryMap.containsKey("result_path")){
            File file = new File(queryMap.get("result_path"));
            PrintWriter printWriter = new PrintWriter (file);
            printWriter.println (classificationStatistics);
            printWriter.close ();
        }

    }



}
