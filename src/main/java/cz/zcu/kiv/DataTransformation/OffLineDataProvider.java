package cz.zcu.kiv.DataTransformation;

import cz.zcu.kiv.signal.*;
import cz.zcu.kiv.Utils.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.ByteOrder;
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
 * OffLineDataProvider, 2017/05/25 22:05 Dorian Beganovic
 *
 **********************************************************************************************************************/

public class OffLineDataProvider {

    //
    private String vhdrFile;
    private String vmrkFile;
    private String eegFile;
    //
    private int FZIndex;
    private int CZIndex;
    private int PZIndex;
    //
    private Map<String, Integer> files = new LinkedHashMap<>();
    private FileSystem fs;
    //
    private List<double[][]> epochs = new ArrayList<>();
    private List<Double> targets = new ArrayList<>();
    private int numberOfTargets = 0;
    private int numberOfNonTargets = 0;
    private int epochsCounter = 0;
    //
    private String filePrefix = "";
    //
    private static Log logger = LogFactory.getLog(OffLineDataProvider.class);


    private String[] args;
    /**
     *
     * @param args The arguments for the program in following format:
     *  -> 1) < location of a .eeg file> < target number>  *< optional values>
     *          - it will extract the .vhdr and .vmrk file
     *          by substituting .eeg suffix with the appropriate
     *  -> 2) < location of info.txt file>
     *          - it will loop over all of the files and also store the guessed number
     * @throws Exception in case the file does not exist
     */
    public OffLineDataProvider(String[] args) throws Exception {
        this.args = args;
        logger.info("Started OffLineDataProvider with arguments" + Arrays.toString(args));
    }

    /**
     * loads the data by doing two things:
     * 1. parsing the input format (.txt vs .eeg)
     * 2. processing each of the files parsed from input
     */
    public void loadData()  {
        try {
            this.fs = FileSystem.get(URI.create(Const.HDFS_URI), Const.HDFS_CONF);
            logger.info("Parsing the input...");
            handleInput(args);
            logger.info("Processing files...");
            processEEGFiles();
        } catch (Exception e) {
            logger.fatal(e.getMessage());
        }
    }

    /**
     * Handles the inputted file location with this logic:
     * - Required input:
     *  -> 1) < location of a .eeg file> < target number>  *< optional values>
     *          - it will extract the .vhdr and .vmrk file
     *          by substituting .eeg suffix with the appropriate
     *  -> 2) < location of info.txt file>
     *          - it will loop over all of the files and also store the guessed number
     * @param args the arguments provided at application startup
     * @throws IOException if the directory doesn't contain the file, or the file does not exists
     */
    private void handleInput(String[] args) throws IOException {
        if(args.length <= 0 || args.length > 6){
            throw new IllegalArgumentException(
                    "Please enter the input in one of these formats: " +
                            "1. <location of info.txt file> " +
                            "2. <location of a .eeg file> <guessed number>  *<optional values>");
        }
        else {
            String fileLocation = args[0];
            // .EEG
            if (fileLocation.substring(fileLocation.length() - 4).equals(".eeg")){ // in this case we have a .eeg file
                logger.info("Input file is .eeg file with location " + fileLocation);
                filePrefix = Const.HadoopUserPrefixFolder;
                logger.info("This prefix will be added to the file" + filePrefix);
                files.put(fileLocation,Integer.parseInt(args[1]));
            }
            // .TXT
            else if(fileLocation.substring(fileLocation.length() - 4).equals(".txt")){
                filePrefix = Const.HadoopUserPrefixFolder + Const.Data_folder;
                logger.info("This prefix will be added to the file" + filePrefix);
                loadFilesFromInfoTxt(fileLocation);
            }
            // DIRECTORY
            else{
                throw new IllegalArgumentException(
                        "Please enter the input in one of these formats: " +
                                "1. <location of info.txt file> " +
                                "2. <location of a .eeg file> <target number>  *<optional values>");
            }
        }
    }

    /**
     * helper function which processes the EEGFiles after
     * @throws Exception in case a file does not exist
     */
    private void processEEGFiles() throws Exception {

        for (Map.Entry<String, Integer> fileEntry: files.entrySet()) {

            try {
                setFileNames(filePrefix + fileEntry.getKey());
            }
            catch (IOException e){
                throw new IllegalArgumentException("The folder you inputted does not exist");
            }
            catch (IllegalArgumentException e){
                logger.error("Did not load the file " + fileEntry.getKey() + " because of this error: ");
                logger.error(e.getMessage());
                continue;
            }
            logger.info("Processing file " + fileEntry.getKey());
            logger.info("Loaded .eeg file:" + eegFile);
            logger.info("Loaded .vdhr file:" + vhdrFile);
            logger.info("Loaded .vmrk file:" + vmrkFile);

            DataTransformer dt = new EEGDataTransformer();
            List<ChannelInfo> channels = dt.getChannelInfo(vhdrFile);

            logger.debug("Extracting channels");

            for (ChannelInfo channel : channels) {

                logger.debug("Channel " + channel.getName() + " " + channel.getUnits());
                if ("fz".equals(channel.getName().toLowerCase())) {
                    FZIndex = channel.getNumber();
                } else if ("cz".equals(channel.getName().toLowerCase())) {
                    CZIndex = channel.getNumber();
                }
                if ("pz".equals(channel.getName().toLowerCase())) {
                    PZIndex = channel.getNumber();
                }
            }

            ByteOrder order = ByteOrder.LITTLE_ENDIAN;
            double[] fzChannel = dt.readBinaryData(vhdrFile, eegFile, FZIndex, order);
            double[] czChannel = dt.readBinaryData(vhdrFile, eegFile, CZIndex, order);
            double[] pzChannel = dt.readBinaryData(vhdrFile, eegFile, PZIndex, order);


            logger.debug("Extracted fzChannel length" + fzChannel.length);
            logger.debug("Extracted czChannel length" + czChannel.length);
            logger.debug("Extracted pzChannel length" + pzChannel.length);


            List<EEGMarker> markers = dt.readMarkerList(vmrkFile);
            //Collections.shuffle(markers);

            // store all epochs in a single list
            for (EEGMarker marker : markers) {

                logger.debug("Using marker " + marker.getName());
                // initiate the epoch object
                EpochHolder epoch = new EpochHolder();

                // 1. set the stimulus index
                String stimulusNumber = marker.getStimulus().replaceAll("[\\D]", "");


                int stimulusIndex = -1;
                if (stimulusNumber.length() > 0) {
                    stimulusIndex = Integer.parseInt(stimulusNumber) - 1;
                }
                epoch.setStimulusIndex(stimulusIndex);
                logger.debug("Set the  stimulus index (" + stimulusNumber + ") of epoch");


                try {
                    //2. set the values of signals for the epochs
                    float[] ffzChannel = DataProviderUtils.toFloatArray(Arrays.copyOfRange(fzChannel,
                            marker.getPosition() - Const.PREESTIMULUS_VALUES, marker.getPosition() + Const.POSTSTIMULUS_VALUES));
                    float[] fczChannel = DataProviderUtils.toFloatArray(Arrays.copyOfRange(czChannel,
                            marker.getPosition() - Const.PREESTIMULUS_VALUES, marker.getPosition() + Const.POSTSTIMULUS_VALUES));
                    float[] fpzChannel = DataProviderUtils.toFloatArray(Arrays.copyOfRange(pzChannel,
                            marker.getPosition() - Const.PREESTIMULUS_VALUES, marker.getPosition() + Const.POSTSTIMULUS_VALUES));

                    Baseline.correct(ffzChannel, Const.PREESTIMULUS_VALUES);
                    Baseline.correct(fczChannel, Const.PREESTIMULUS_VALUES);
                    Baseline.correct(fpzChannel, Const.PREESTIMULUS_VALUES);

                    epoch.setFZ(ffzChannel, 100);
                    epoch.setCZ(fczChannel, 100);
                    epoch.setPZ(fpzChannel, 100);

                    logger.debug("Set the ffz, fcz and fpz channels of epoch");

                    // 3. try to set the target value ???
                    if (epoch.getStimulusIndex() + 1 == fileEntry.getValue()) {
                        epoch.setTarget(true);
                    }

                    logger.debug("Set the target label of epoch");

                    epochsCounter++;
                    logger.debug("Epoch " + epochsCounter + " has been processed");

                    // 1 = target, 3 = non-target
                    if (epoch.isTarget() && numberOfTargets <= numberOfNonTargets) {
                        logger.debug("Epoch is a positive target and numberOfTargets <= numberOfNonTargets");
                        logger.debug("Training data increased \n");
                        this.epochs.add(epoch.getEpoch());
                        this.targets.add(1.0);
                        this.numberOfTargets++;
                    } else if (!epoch.isTarget() && numberOfTargets >= numberOfNonTargets) {
                        logger.debug("Epoch isn't target and numberOfTargets >= numberOfNonTargets");
                        logger.debug("Training data increased \n");
                        this.epochs.add(epoch.getEpoch());
                        this.targets.add(0.0);
                        this.numberOfNonTargets++;
                    }

                } catch (ArrayIndexOutOfBoundsException ex) {
                    logger.error("Array index out of bounds, skipping this epoch");
                }
            }
        }

    }



    /**
     * Handles the loading of info.txt files where the format is
     * eg.
     * "Stankov/Stankov_2014_01.egg 2
     *  Stankov/Stankov_2014_02.egg 2
     *  Stankov/Stankov_2014_03.egg 2"
     *
     * @param fileLocation the location of .txt file with above specified format
     * @return Map with key=location of .eeg file, value = guessed number
     * @throws IOException in case the .txt or one of specified .eeg file does not exist
     */
    private void loadFilesFromInfoTxt(String fileLocation) throws IOException {
        logger.info("Reading file from info.txt file");
        // init a new reader for hadoop hdfs
        BufferedReader br=new BufferedReader(new InputStreamReader(fs.open(new Path(fileLocation))));
        String line;
        int num;
        String fileLoc;
        // read all lines
        while ((line = br.readLine()) != null) {
            // if the line is empty, ignore it
            if (line.length() == 0){
                continue;
            }
            // if the line starts with #, ignore it
            if (line.charAt(0) == '#') { //comment line in info txt
                continue;
            }
            // each line is expected to be <file location*.eeg > < guessed number> <Optional vals>
            String[] parts = line.split(" ");
            if (parts.length > 1) {
                try {
                    fileLoc  = parts[0];
                    num = Integer.parseInt(parts[1]);
                    // store in the map
                    files.put(fileLoc, num);
                    logger.info("Stored file " + fileLoc);
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Line " + line + " contains an improper number format");
                }
            }
        }
        br.close();
    }

    /**
     *
     * @param dataLocation the file must be ending in .eeg format
     * @throws IllegalArgumentException in case the algorithm was not found
     * @throws IOException in case there was an IO issue
     */
    private void setFileNames(String dataLocation) throws IllegalArgumentException, IOException {
        if (dataLocation.length() <= 4) {
            throw new IllegalArgumentException("Incorrect file name, must be at least longer than 4 characters ") ;
        }
        else {
            String eegFileLocation = "";
            if (dataLocation.substring(dataLocation.length() - 4).equals(".eeg")){ // in this case we have a .eeg file
                eegFileLocation = dataLocation;
            }
            else { // here we assume a folder/directory is given and we have to find the .eeg file
                if(eegFileLocation.length() <= 2){
                    throw new IllegalArgumentException("There is no .eeg file in the given directory");
                }
                throw new IllegalArgumentException("Invalid .eeg file");
            }

            // based on the .eeg file, change just the suffixes
            int index = eegFileLocation.lastIndexOf(".");
            String baseName = eegFileLocation.substring(0, index);
            String vhdrFileLocation = baseName + Const.VHDR_EXTENSION;
            String vmrkFileLocation = baseName + Const.VMRK_EXTENSION;

            // check that all of the data actually exists
            if (!fs.exists(new Path(vhdrFileLocation))){
                throw new IllegalArgumentException("No related .vhdr file found for the original .eeg file " + vhdrFileLocation);
            }
            if (!fs.exists(new Path(vmrkFileLocation))){
                throw new IllegalArgumentException("No related .vmrk file found for the original .eeg file " + vmrkFileLocation);
            }
            if (!fs.exists(new Path(eegFileLocation))){
                throw new IllegalArgumentException("No related .eeg file found for the original .eeg file " + eegFileLocation);
            }

            // finally, set the property
            this.vhdrFile = vhdrFileLocation;
            this.vmrkFile = vmrkFileLocation;
            this.eegFile = eegFileLocation;
        }
    }

    /**
     * @return epochs for training
     */
    public List<double[][]> getData() {
        return this.epochs;
    }

    /**
     * @return data labels
     */
    public List<Double> getDataLabels(){
        return this.targets;
    }
}
