package cz.zcu.kiv.Classification;

import com.twelvemonkeys.util.LinkedSet;
import cz.zcu.kiv.FeatureExtraction.IFeatureExtraction;
import cz.zcu.kiv.Utils.ClassificationStatistics;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/***********************************************************************************************************************
 *
 * This file is part of the EEG_Analysis project

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
 * NeuralNetworkClassifier, 2017/08/08 11:55 dbg
 *
 **********************************************************************************************************************/
public class NeuralNetworkClassifier implements IClassifier {

    private static Log logger = LogFactory.getLog(NeuralNetworkClassifier.class);
    public static IFeatureExtraction fe;
    private static MultiLayerNetwork model;
    private HashMap<String,String> config;

    @Override
    public void setFeatureExtraction(IFeatureExtraction fe) {
        NeuralNetworkClassifier.fe = fe;
    }

    @Override
    public void train(List<double[][]> epochs, List<Double> targets, IFeatureExtraction fe) {
        NeuralNetworkClassifier.fe = fe;
        // Customizing params of classifier
        final int numFeatures = fe.getFeatureDimension();   // number of targets on a line
        final int numLabels = 2;   // number of labels needed for classifying

        //Load Data - when target is 0, label[0] is 0 and label[1] is 1.
        double[][] labels = new double[targets.size()][numLabels]; // Matrix of labels for classifier
        double[][] features_matrix = new double[targets.size()][numFeatures]; // Matrix of features
        for (int i = 0; i < epochs.size(); i++) { // Iterating through epochs
            double[][] epoch = epochs.get(i); // Each epoch
            double[] features = fe.extractFeatures(epoch); // Feature of each epoch
            for (int j = 0; j < numLabels; j++) {   //setting labels for each column
                labels[i][0] = targets.get(i); // Setting label on position 0 as target
                labels[i][1] = Math.abs(1 - targets.get(i));  // Setting label on position 1 to be different from label[0]
            }
            features_matrix[i] = features; // Saving features to features matrix
        }

        // Creating INDArrays and DataSet
        INDArray output_data = Nd4j.create(labels); // Create INDArray with labels(targets)
        INDArray input_data = Nd4j.create(features_matrix); // Create INDArray with features(data)
        DataSet dataSet = new DataSet(input_data, output_data); // Create dataSet with features and labels
        Nd4j.ENFORCE_NUMERICAL_STABILITY = true; // Setting to enforce numerical stability



        /*
        parsing the parameters
        */

        logger.info("Config is " + config);

        NeuralNetConfiguration.ListBuilder builder = new NeuralNetConfiguration.Builder()
                .seed(Integer.parseInt(config.get("config_seed")))
                .iterations(Integer.parseInt(config.get("config_num_iterations")))
                .learningRate(Double.parseDouble(config.get("config_learning_rate")))
                .momentum(Double.parseDouble(config.get("config_momentum")))
                .weightInit(parseWeightInit(config.get("config_weight_init")))
                .updater(parseUpdater(config.get("config_updater")))
                .optimizationAlgo(parseOptimizationAlgo(config.get("config_optimization_algo")))
                .list();

        logger.info("Managed to set the basic configuration ");

        int numLayers = 0;
        for (String key : config.keySet()){
            if(key.startsWith("config_layer")){
                numLayers++;
            }
        }
        numLayers = numLayers/4;
        for (int id = 1; id <= numLayers; id++ ){
            builder.layer(id-1, parseLayer(id,config.get("config_layer" + id + "_layer_type")));
        }
        logger.info("Number of layers " + numLayers);

        if(config.get("config_pretrain").equals("true")){
            builder.pretrain(true);
        }
        else{
            builder.pretrain(false);
        }
        if(config.get("config_backprop").equals("true")){
            builder.backprop(true);
        }
        else{
            builder.backprop(false);
        }

        MultiLayerConfiguration conf = builder.build();

        model = new MultiLayerNetwork(conf); // Passing built configuration to instance of multilayer network
        model.init(); // Initialize mode
        //model.printConfiguration();
        logger.info("Started training the classifier");
        model.fit(dataSet);
        logger.info("Trained the classifier");
        //model.setListeners(new ScoreIterationListener(100));// Setting listeners

    }

    @Override
    public ClassificationStatistics test(List<double[][]> epochs, List<Double> targets) {
        logger.info("Testing the classifier");
        ClassificationStatistics resultsStats = new ClassificationStatistics(); // initialization of classifier statistics
        logger.info(epochs.size());
        for (int i = 0; i < epochs.size(); i++) {   //iterating epochs
            logger.info(i);
            double[] featureVector = fe.extractFeatures(epochs.get(i)); // Extracting features to vector
            INDArray features = Nd4j.create(featureVector); // Creating INDArray with extracted features
            logger.debug("Extracted the features");
            logger.debug("Using the classifier");
            double output = model.output(features, org.deeplearning4j.nn.api.Layer.TrainingMode.TEST).getDouble(0); // Result of classifying
            logger.debug("Prediction is " + output);
            resultsStats.add(output, targets.get(i));   // calculating statistics
        }
        logger.info("Done testing the classifier");
        return resultsStats;    //  returns classifier statistics
    }

    @Override
    public void save(String file) throws IOException{
        new File(file).delete();
        ModelSerializer.writeModel(model,file,false);
        logger.info("Saved the classifier");
    }

    @Override
    public void load(String file) {
        try {
            model = ModelSerializer.restoreMultiLayerNetwork(file);
            if (model != null){
                logger.info("Loaded the classifier");
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public IFeatureExtraction getFeatureExtraction() {
        return fe;
    }

    @Override
    public void setConfig(HashMap<String, String> config) {
        this.config = config;
    }



    private WeightInit parseWeightInit(String command){
        switch (command){
            case "xavier" : return WeightInit.XAVIER;
            case "zero" : return WeightInit.ZERO;
            case "sigmoid" : return WeightInit.SIGMOID_UNIFORM;
            case "uniform" : return WeightInit.UNIFORM;
            case "relu" : return WeightInit.RELU;
            default: return WeightInit.RELU;
        }
    }

    private Updater parseUpdater(String command){
        switch (command){
            case "sgd" : return Updater.SGD;
            case "adam" : return Updater.ADAM;
            case "nesterovs" : return Updater.NESTEROVS;
            case "adagrad" : return Updater.ADAGRAD;
            case "rmsprop" : return Updater.RMSPROP;
            default: return Updater.NESTEROVS;
        }
    }

    private OptimizationAlgorithm parseOptimizationAlgo(String command){
        switch (command){
            case "line_gradient_descent" : return OptimizationAlgorithm.LINE_GRADIENT_DESCENT;
            case "lbfgs" : return OptimizationAlgorithm.LBFGS;
            case "conjugate_gradient" : return OptimizationAlgorithm.CONJUGATE_GRADIENT;
            case "stochastic_gradient_descent" : return OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT;
            default: return OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT;
        }
    }

    private LossFunctions.LossFunction parseLossFunction(String command){
        switch (command){
            case "mse" : return LossFunctions.LossFunction.MSE;
            case "xent" : return LossFunctions.LossFunction.XENT;
            case "squared_loss" : return LossFunctions.LossFunction.SQUARED_LOSS;
            case "negativeloglikelihood" : return LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD;
            default: return LossFunctions.LossFunction.MSE;
        }
    }


    private Activation parseActivation(String command){
        switch (command){
            case "sigmoid" : return Activation.SIGMOID;
            case "softmax" : return Activation.SOFTMAX;
            case "relu" : return Activation.RELU;
            case "tanh" : return Activation.TANH;
            case "identity" : return Activation.IDENTITY;
            case "softplus" : return Activation.SOFTPLUS;
            case "elu" : return Activation.ELU;
            default: return Activation.SIGMOID;
        }
    }


    private Layer parseLayer(int id, String command){
        int nIn;
        if(id == 1){
            nIn = fe.getFeatureDimension();
        }
        else if (id >= 2){
            nIn = Integer.parseInt(config.get("config_layer"+(id-1)+"_n_out"));
        }
        else {
            throw new IllegalArgumentException("Cannot configure layer 0");
        }
        switch (command){
            case "output" : return
                    new OutputLayer.Builder()
                            .nIn(nIn)
                            .nOut(Integer.parseInt(config.get("config_layer" + (id) + "_n_out")))
                            .dropOut(Double.parseDouble(config.get("config_layer"+ (id) + "_drop_out")))
                            .activation(parseActivation(config.get("config_layer" + (id) + "_activation_function")))
                            .lossFunction(parseLossFunction(config.get("config_loss_function")))
                            .build()
                    ;
            case "dense" : return
                    new DenseLayer.Builder()
                            .nIn(nIn)
                            .nOut(Integer.parseInt(config.get("config_layer" + (id) + "_n_out")))
                            .dropOut(Double.parseDouble(config.get("config_layer"+ (id) + "_drop_out")))
                            .activation(parseActivation(config.get("config_layer" + (id) + "_activation_function")))
                            .build()
                    ;
            case "auto_encoder" : return
                    new AutoEncoder.Builder()
                            .nIn(nIn)
                            .nOut(Integer.parseInt(config.get("config_layer" + (id) + "_n_out")))
                            .dropOut(Double.parseDouble(config.get("config_layer"+ (id) + "_drop_out")))
                            .activation(parseActivation(config.get("config_layer" + (id) + "_activation_function")))
                            .build()
                    ;
            case "rbm" : return
                    new RBM.Builder()
                            .nIn(nIn)
                            .nOut(Integer.parseInt(config.get("config_layer" + (id) + "_n_out")))
                            .dropOut(Double.parseDouble(config.get("config_layer"+ (id) + "_drop_out")))
                            .activation(parseActivation(config.get("config_layer" + (id) + "_activation_function")))
                            .build()
                    ;
            case "graves_lstm" : return
                    new GravesLSTM.Builder()
                            .nIn(nIn)
                            .nOut(Integer.parseInt(config.get("config_layer" + (id) + "_n_out")))
                            .dropOut(Double.parseDouble(config.get("config_layer"+ (id) + "_drop_out")))
                            .activation(parseActivation(config.get("config_layer" + (id) + "_activation_function")))
                            .build()
                    ;
            default: return
                    new OutputLayer.Builder()
                            .nIn(nIn)
                            .nOut(Integer.parseInt(config.get("config_layer" + (id) + "_n_out")))
                            .activation(parseActivation(config.get("config_layer" + (id) + "_activation_function")))
                            .lossFunction(parseLossFunction(config.get("config_loss_function")))
                            .build()
                    ;
        }
    }

}
