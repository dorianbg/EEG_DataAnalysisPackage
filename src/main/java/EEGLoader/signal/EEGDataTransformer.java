package EEGLoader.signal;


import EEGLoader.signal.DataTransformer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;

import java.io.*;
import java.net.URI;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;

public class EEGDataTransformer implements DataTransformer {

    private VhdrReader reader = new VhdrReader();
    /**
     * This method reads binary data and decodes double values.
     * This method expects data file and header file in binary form. It is not depend on data source.
     * Important! The header file must be a first parameter, the data file must be a second parameter.
     *
     * @param headerFile - the header file in binary representation.
     * @param dataFile - the data file in binary representation.
     * @param channel - number of channel, used electrode
     * @return double values of EEG signal.
     */
    public double[] readBinaryData(byte[] headerFile, byte[] dataFile, int channel, ByteOrder order) {
        reader.readVhdr(headerFile);
        EegReader eeg = new EegReader(reader);
        return eeg.readFile(dataFile, channel, order);
    }

    /**
         * This method reads binary data and decodes double values.
         * This method expect paths of header and data file in form of String.
         * Important! The header file must be a first parameter, the data file must be a second parameter.
         *
         * @param headerFile  - the path to the header file
         * @param dataFile - the path to the data file
         * @param channel - number of channel, used electrode
         * @return double values of EEG signal
         * @throws IOException
         */
    public double[] readBinaryData(String headerFile, String dataFile, int channel, ByteOrder order) throws IOException {
        return readBinaryData(fileToByteArray(headerFile), fileToByteArray(dataFile), channel, order);
    }

    /**
         * This method reads binary data and decodes double values.
         * This method expects path of the header file. It also expects that the data file is in the same directory.
         * The header file contains information about the data file. If it is in the same directory,
         * this method is able to find and read it.
         *
         * @param headerFile - the path to the header file
         * @param channel - number of channel, used electrode
         * @return  double values of EEG signal
         * @throws IOException
         */
    public double[] readBinaryData(String headerFile, int channel, ByteOrder order) throws IOException {
        String dataFile = getEEGFileName(headerFile);
        return readBinaryData(headerFile, dataFile, channel, order);
    }

    /**
         * This method reads the marker file containing information about stimuli.
         *
         * @param markerFile - the path to the marker file
         * @return map of stimuli. Each EEGMarker includes information about stimuli, its name and position.
         */
    public HashMap<String, EEGMarker> readMarkers(String markerFile) throws IOException {
        reader.readVmrk(fileToByteArray(markerFile));
        return reader.getMarkers();
    }

    @Override
    public List<EEGMarker> readMarkerList(String markerFile) throws IOException {
        reader.readVmrk(fileToByteArray(markerFile));
        return reader.getMarkerList();
    }

    /**
         * This method provides loaded properties of data file from header file.
         *
         *
         * @return properties in HashMap
         */
    public HashMap<String, HashMap<String, String>> getProperties() {
        return reader.getProperties();
    }

    /**
         * This method provides the list of channel obtained from header file.
         *
         *
         * @return The list of channels
         */
    public List<ChannelInfo> getChannelInfo() {
        return reader.getChannels();
    }

    private byte[] fileToByteArray(String filename) throws IOException {
        /*File file = new File(filename);
        InputStream fileIS = new FileInputStream(file);
        DataInputStream data = new FSDataInputStream(fileIS);
        */
        String uri = "hdfs://localhost:8020";
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(URI.create(uri), conf);
        FSDataInputStream data = fs.open(new Path(filename));


        byte[] fileArray = new byte[(int) fs.getFileLinkStatus(new Path(filename)).getLen()];
        data.readFully(fileArray);
        return fileArray;
    }

    private String getEEGFileName(String header) throws IOException {
        reader.readVhdr(fileToByteArray(header));
        int index = header.lastIndexOf("\\");
        String dir = header.substring(0, index + 1);
        HashMap<String, HashMap<String, String>> properties = reader.getProperties();
        HashMap<String, String> property = properties.get("CI");

        return dir + property.get("DataFile");
    }

    @Override
    public List<ChannelInfo> getChannelInfo(String headerFile) throws IOException {
        reader.readVhdr(fileToByteArray(headerFile));
        return getChannelInfo();
    }
}
