package OffLineDataProvider;

import EEGLoader.ChannelInfo;
import EEGLoader.DataTransformer;
import EEGLoader.EEGDataTransformer;
import EEGLoader.EEGMarker;
import Utils.Baseline;
import Utils.Const;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.ByteOrder;
import java.util.*;

/**
 * Created by Dorian Beganovic on 15/05/2017.
 */
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
    private Map<String, Integer> files = new HashMap<>();
    private FileSystem fs;
    //
    private List<double[][]> epochs = new ArrayList<>();
    private List<Double> targets = new ArrayList<>();
    private int numberOfTargets = 0;
    private int numberOfNonTargets = 0;
    //
    private String filePrefix = "";


    public OffLineDataProvider(String[] args) throws Exception {
        this.fs = FileSystem.get(URI.create(Const.HDFS_URI), Const.HDFS_CONF);
        handleInput(args);
        loadData();
    }

    private void loadData() throws Exception {

        for (Map.Entry<String, Integer> fileEntry: files.entrySet()) {

            try {
                setFileNames(filePrefix + fileEntry.getKey());
            }
            catch (IOException e){
                throw new IllegalArgumentException("the folder you inputted does not exist");
            }
            catch (IllegalArgumentException e){
                System.out.println("Did not load the file " + fileEntry.getKey() + " because of this error: ");
                System.out.println(e.getMessage());
                continue;
            }
            /*
            System.out.println(vhdrFile);
            System.out.println(vmrkFile);
            System.out.println(eegFile);
            */

            DataTransformer dt = new EEGDataTransformer();
            List<ChannelInfo> channels = dt.getChannelInfo(vhdrFile);
            for (ChannelInfo channel : channels) {

                // System.out.println(channel.getName() + " " + channel.getUnits());

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

            /*
            System.out.println(fzChannel.length);
            System.out.println(czChannel.length);
            System.out.println(pzChannel.length);
            */

            List<EEGMarker> markers = dt.readMarkerList(vmrkFile);
            //Collections.shuffle(markers);

            // store all epochs in a single list

            for (EEGMarker marker : markers) {
                //System.out.println(marker);

                // initiate the epoch object
                EpochHolder epoch = new EpochHolder();

                // 1. set the stimulus index
                String stimulusNumber = marker.getStimulus().replaceAll("[\\D]", "");
                int stimulusIndex = -1;
                if (stimulusNumber.length() > 0) {
                    stimulusIndex = Integer.parseInt(stimulusNumber) - 1;
                }
                epoch.setStimulusIndex(stimulusIndex);


                try {
                    //2. set the values of signals for the epochs
                    float[] ffzChannel = Utils.toFloatArray(Arrays.copyOfRange(fzChannel,
                            marker.getPosition() - Const.PREESTIMULUS_VALUES, marker.getPosition() + Const.POSTSTIMULUS_VALUES));
                    float[] fczChannel = Utils.toFloatArray(Arrays.copyOfRange(czChannel,
                            marker.getPosition() - Const.PREESTIMULUS_VALUES, marker.getPosition() + Const.POSTSTIMULUS_VALUES));
                    float[] fpzChannel = Utils.toFloatArray(Arrays.copyOfRange(pzChannel,
                            marker.getPosition() - Const.PREESTIMULUS_VALUES, marker.getPosition() + Const.POSTSTIMULUS_VALUES));

                    Baseline.correct(ffzChannel, Const.PREESTIMULUS_VALUES);
                    Baseline.correct(fczChannel, Const.PREESTIMULUS_VALUES);
                    Baseline.correct(fpzChannel, Const.PREESTIMULUS_VALUES);

                    epoch.setFZ(ffzChannel, 100);
                    epoch.setCZ(fczChannel, 100);
                    epoch.setPZ(fpzChannel, 100);


                    // 3. try to set the target value ???
                    //System.out.println(epoch.getStimulusIndex());
                    if (epoch.getStimulusIndex() + 1 == fileEntry.getValue()) {
    //                            System.out.println(em.getStimulusIndex());
                        epoch.setTarget(true);
                    }
                    // 1 = target, 3 = non-target
                    if (epoch.isTarget() && numberOfTargets <= numberOfNonTargets) {
                        this.epochs.add(epoch.getEpoch());
                        this.targets.add(1.0);
                        this.numberOfTargets++;
                    } else if (!epoch.isTarget() && numberOfTargets >= numberOfNonTargets) {
                        this.epochs.add(epoch.getEpoch());
                        this.targets.add(0.0);
                        this.numberOfNonTargets++;
                    }

                } catch (ArrayIndexOutOfBoundsException ex) {
                    ex.printStackTrace();
                }


            }
        }
    }


    private void handleInput(String[] args) throws IOException {
        if(args.length <= 0 || args.length > 6){
            throw new IllegalArgumentException(
                    "Please enter the input in one of these formats: " +
                            "1. <location of info.txt file> " +
                            "2. <location of a directory containing a .eeg file> <target number> *<optional values>" +
                            "3. <location of a .eeg file> <target number>  *<optional values>");
        }
        else {
            String fileLocation = args[0];
            if (fileLocation.substring(fileLocation.length() - 4).equals(".eeg")){ // in this case we have a .eeg file
                filePrefix = Const.HadoopUserPrefixFolder;
                files.put(fileLocation,Integer.parseInt(args[1]));
            }
            else if(fileLocation.substring(fileLocation.length() - 4).equals(".txt")){
                filePrefix = Const.HadoopUserPrefixFolder + Const.Data_folder;
                files = loadExpectedFiles(fileLocation);
            }
            else{
                filePrefix = Const.HadoopUserPrefixFolder;
                files.put(fileLocation,Integer.parseInt(args[1]));
            }

        }


    }

    private Map<String, Integer> loadExpectedFiles(String fileLocation) throws IOException {
        Map<String, Integer> res = new HashMap<>();

        BufferedReader br=new BufferedReader(new InputStreamReader(fs.open(new Path(fileLocation))));
        System.out.println(fileLocation);
        String line;
        int num;
        String fileLoc;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
            if (line.length() == 0){
                continue;
            }
            if (line.charAt(0) == '#') { //comment in info txt
                continue;
            }
            String[] parts = line.split(" ");
            if (parts.length > 1) {
                try {
                    num = Integer.parseInt(parts[1]);
                    fileLoc  = parts[0];
                    res.put(fileLoc, num);
                } catch (NumberFormatException ex) {
                    //NaN
                }
            }
        }

        br.close();
        return res;

    }


    public List<double[][]> getTrainingData() {
        return this.epochs;
    }
    public List<Double> getTrainingDataLabels(){
        return this.targets;
    }
    /*
    This will handle the users input in terms of the provided input
    1) if given a folder, that there are .eeg and the related .vmrk and .vhdr files
    2) if given a filename (must end with .eeg), that there are .vmrk and .vhdr files
     */
    private void setFileNames(String dataLocation) throws IllegalArgumentException, IOException {
        if (dataLocation.length() <= 4) {
            throw new IllegalArgumentException("Incorrect file name, must be atleast longer than 4 characters ") ;
        }
        else {
            String eegFileLocation = "";
            if (dataLocation.substring(dataLocation.length() - 4).equals(".eeg")){ // in this case we have a .eeg file
                eegFileLocation = dataLocation;
            }
            else if(dataLocation.substring(dataLocation.length() - 4).equals(".txt")){
                // this case should never happen
                throw new IllegalArgumentException("Incorrect file name") ;
            }
            else { // here we assume a folder/directory is given and we have to find the .eeg file
                FileStatus[] status = fs.listStatus(new Path(dataLocation));
                Path[] listedPaths = FileUtil.stat2Paths(status);
                // here we go over all the files in the directory and search for one ending in .eeg
                for (Path p : listedPaths) {
                    //System.out.println(p.toString());
                    if(p.toString().substring(p.toString().length() - 4).equals(".eeg")){
                        eegFileLocation = p.toString().replace(Const.HDFS_URI,"");
                        break;
                    }
                }
                // we just have to do a check that indeed we found an eeg file
                if(eegFileLocation.length() <= 2){
                    throw new IllegalArgumentException("There is no .eeg file in the given directory");
                }
                // this will set the private fields for file names
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



}
