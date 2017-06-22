package cz.zcu.kiv.FeatureExtraction;

import cz.zcu.kiv.Utils.Const;
import cz.zcu.kiv.Utils.SignalProcessing;
import cz.zcu.kiv.eegdsp.common.ISignalProcessingResult;
import cz.zcu.kiv.eegdsp.common.ISignalProcessor;
import cz.zcu.kiv.eegdsp.main.SignalProcessingFactory;
import cz.zcu.kiv.eegdsp.wavelet.discrete.WaveletResultDiscrete;
import cz.zcu.kiv.eegdsp.wavelet.discrete.WaveletTransformationDiscrete;
import cz.zcu.kiv.eegdsp.wavelet.discrete.algorithm.wavelets.WaveletDWT;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.function.Function;


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
 * WaveletTransform, 2017/06/11 15:39 Dorian Beganovic
 *
 **********************************************************************************************************************/
public class WaveletTransform implements IFeatureExtraction {
    private static Log logger = LogFactory.getLog(WaveletTransform.class);

    private static final long serialVersionUID = 7526472295622776147L;
    /**
     * EEG channels to be transformed to feature vectors
     */
    private static  final int[] CHANNELS = { 1, 2, 3 };

    /**
     * Number of samples to be used - Fs = 1000 Hz expected
     */
    private int EPOCH_SIZE = 512;

    /**
     * Subsampling factor
     */
    private static final int DOWN_SMPL_FACTOR = 1;

    /**
     * Skip initial samples in each epoch
     */
    private int SKIP_SAMPLES = 175;

    /**
     * Name of the wavelet
     */
    private int NAME;

    /**
     * Size of feature vector
     */
    private int FEATURE_SIZE = 16;

    /**
     * Constructor for the wavelet transform feature extraction with default
     * wavelet
     */
    public WaveletTransform() {
        this.NAME = 8;
    }

    public WaveletTransform(int name, int epochSize, int skipSamples, int featureSize) {
        this.NAME = name;
        this.EPOCH_SIZE = epochSize;
        this.SKIP_SAMPLES = skipSamples;
        this.FEATURE_SIZE = featureSize;
    }

    /**
     * Constructor for the wavelet transform feature extraction with user
     * defined wavelet
     *
     * @param name
     *            - name of the wavelet transform method
     */
    public WaveletTransform(int name) {
        setWaveletName(name);
    }

    /**
     * Method that creates a wavelet by a name using SignalProcessingFactory and
     * processes the signal
     *
     * @param epoch
     *            - source epochs
     * @return - normalized feature vector with only approximation coefficients
     */
    @Override
    public double[] extractFeatures(double[][] epoch) {
        ISignalProcessor dwt = SignalProcessingFactory.getInstance()
                .getWaveletDiscrete();
        String[] names = ((WaveletTransformationDiscrete) dwt)
                .getWaveletGenerator().getWaveletNames();
        WaveletDWT wavelet = null;
        try {
            wavelet = ((WaveletTransformationDiscrete) dwt)
                    .getWaveletGenerator().getWaveletByName(names[NAME]);
        } catch (Exception e) {
            logger.info("Exception loading wavelet " + names[NAME] + ".");
        }
        ((WaveletTransformationDiscrete) dwt).setWavelet(wavelet);

        ISignalProcessingResult res;
        int numberOfChannels = CHANNELS.length;
        double[] features = new double[FEATURE_SIZE * numberOfChannels];
        int i = 0;
        for (int channel : CHANNELS) {
            double[] currChannelData = new double[EPOCH_SIZE];
            for (int j = 0; j < EPOCH_SIZE; j++) {
                currChannelData[j] = epoch[channel - 1][j + SKIP_SAMPLES];
            }
            res = dwt.processSignal(currChannelData);
            for (int j = 0; j < FEATURE_SIZE; j++) {
                features[i * FEATURE_SIZE + j] = ((WaveletResultDiscrete) res)
                        .getDwtCoefficients()[j];
            }
            i++;
        }
        features = SignalProcessing.normalize(features);

        return features;
    }


    /**
     * Gets feature vector dimension
     *
     * @return - feature vector dimension
     */
    @Override
    public int getFeatureDimension() {
        return FEATURE_SIZE * CHANNELS.length / DOWN_SMPL_FACTOR;
    }

    /**
     * Sets wavelet name
     *
     * @param name
     *            - number that indicates the wavelet name
     */
    public void setWaveletName(int name) {
        if (name >= 0 && name <= 17) {
            this.NAME = name;
        } else
            throw new IllegalArgumentException(
                    "Wavelet Name must be >= 0 and <= 17");
    }

    /**
     * Sets size of epoch to use for feature extraction
     *
     * @param epochSize
     *            - size of epoch to use
     */
    public void setEpochSize(int epochSize) {
        if (epochSize > 0 && epochSize <= Const.POSTSTIMULUS_VALUES) {
            this.EPOCH_SIZE = epochSize;
        } else {
            throw new IllegalArgumentException("Epoch Size must be > 0 and <= "
                    + Const.POSTSTIMULUS_VALUES);
        }
    }

    /**
     * Sets how many initiate samples of epoch to skip
     *
     * @param skipSamples
     *            - number of samples to skip
     */
    public void setSkipSamples(int skipSamples) {
        if (skipSamples > 0 && skipSamples <= Const.POSTSTIMULUS_VALUES) {
            this.SKIP_SAMPLES = skipSamples;
        } else {
            throw new IllegalArgumentException(
                    "Skip Samples must be > 0 and <= "
                            + Const.POSTSTIMULUS_VALUES);
        }
    }

    /**
     * Sets how many coeficients will be used after extracting the feature
     *
     * @param featureSize
     *            - size of feature
     */
    public void setFeatureSize(int featureSize) {
        if (featureSize > 0 && featureSize <= 1024) {
            this.FEATURE_SIZE = featureSize;
        } else {
            throw new IllegalArgumentException(
                    "Feature Size must be > 0 and <= 1024");
        }
    }

    @Override
    public String toString() {
        return "DWT: EPOCH_SIZE: " + this.EPOCH_SIZE +
                " FEATURE_SIZE: " + this.FEATURE_SIZE +
                " WAVELETNAME: " + this.NAME +
                " SKIP_SAMPLES: " + this.SKIP_SAMPLES +
                "\n";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WaveletTransform that = (WaveletTransform) o;

        if (EPOCH_SIZE != that.EPOCH_SIZE) return false;
        if (SKIP_SAMPLES != that.SKIP_SAMPLES) return false;
        if (NAME != that.NAME) return false;
        return FEATURE_SIZE == that.FEATURE_SIZE;

    }

    @Override
    public int hashCode() {
        int result = EPOCH_SIZE;
        result = 31 * result + SKIP_SAMPLES;
        result = 31 * result + NAME;
        result = 31 * result + FEATURE_SIZE;
        return result;
    }

}
