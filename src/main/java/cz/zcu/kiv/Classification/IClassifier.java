package cz.zcu.kiv.Classification;

import cz.zcu.kiv.FeatureExtraction.IFeatureExtraction;
import cz.zcu.kiv.Utils.ClassificationStatistics;

import java.io.InputStream;
import java.io.OutputStream;
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
 * IClassifier, 2017/06/18 14:30 Dorian Beganovic
 *
 **********************************************************************************************************************/
public interface IClassifier {
    /**
     *
     * Predefine feature extraction method
     *
     * @param fe
     */
    void setFeatureExtraction(IFeatureExtraction fe);

    /**
     * Train the classifier using information from the supervizor
     * @param epochs raw epochs  - list of M channels x N time samples
     * @param targets target classes - list of expected classes (0 or 1)
     * @param fe method for feature extraction
     */
    void train(List<double[][]> epochs, List<Double> targets, IFeatureExtraction fe);

    /**
     * Test the classifier using the data with known resulting classes
     * @param epochs raw epochs  - list of M channels x N time samples
     * @param targets target classes - list of expected classes (0 or 1)
     * @return
     */
    ClassificationStatistics test(List<double[][]> epochs, List<Double> targets);


    /**
     *
     * Save the classifier to a model
     * @param file location of the model on PC
     */
    void save(String file);

    /**
     *
     * Load a classifier from a model
     * @param file location of the model on PC
     */
    void load(String file);

    /**
     * Return the feature extractor used
     * @return feature extraction method
     */
    IFeatureExtraction getFeatureExtraction();

}
