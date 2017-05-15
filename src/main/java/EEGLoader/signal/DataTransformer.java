package EEGLoader.signal;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;

/**
 * This interface provides methods for loading and decoding the binary representation of EEG binary data.
 * Create an instance of the class EEGDataTransformer.
 *
 * @author Jan Stebetak
 */
public interface DataTransformer {

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
    public double[] readBinaryData(byte[] headerFile, byte[] dataFile,  int channel, ByteOrder order);

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
    public double[] readBinaryData(String headerFile, String dataFile, int channel, ByteOrder order) throws IOException;

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
    public double[] readBinaryData(String headerFile, int channel, ByteOrder order) throws IOException;


    /**
     * This method reads the marker file containing information about stimuli.
     *
     * @param markerFile - the path to the marker file
     * @return map of stimuli. Each EEGMarker includes information about stimuli, its name and position.
     */
    public HashMap<String, EEGMarker> readMarkers(String markerFile) throws IOException;

    public List<EEGMarker> readMarkerList(String markerFile) throws IOException;

    /**
     * This method provides loaded properties of data file from header file.
     *
     *
     * @return properties in HashMap
     */
    public HashMap<String, HashMap<String, String>> getProperties();

    /**
     * This method provides the list of channel obtained from header file.
     *
     *
     * @return The list of channels
     */
    public List<ChannelInfo> getChannelInfo();

    /**
     * This method provides the list of channel obtained from header file.
     *
     *
     * @return The list of channels
     */
    public List<ChannelInfo> getChannelInfo(String headerFile) throws IOException;
}
