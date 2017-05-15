package EEGLoader.signal;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class EegReader {

    private VhdrReader info;
    private int channelCnt;
    private Class binaryType;
    private byte[] eegData;
    private DataOrientation dataOrientation;

    public EegReader(VhdrReader info) {
        this.info = info;
        channelCnt = Integer.parseInt(info.getProperties().get("CI").get("NumberOfChannels"));
        if (info.getProperties().get("BI").get("BinaryFormat").equals("INT_16")) {
            binaryType = Integer.class;
        }
        if (info.getProperties().get("BI").get("BinaryFormat").equals("IEEE_FLOAT_32")) {
            binaryType = Float.class;
        }
        if (info.getProperties().get("CI").get("DataOrientation").equals("MULTIPLEXED")) {
            dataOrientation = DataOrientation.MULTIPLEXED;
        }
        if (info.getProperties().get("CI").get("DataOrientation").equals("VECTORIZED")) {
            dataOrientation = DataOrientation.VECTORIZED;
        }
    }

    public double[] readFile(byte[] binaryFile, int channel, ByteOrder order) {
        eegData = binaryFile;
        
        return readOneChannel(channel, order);
    }

    private double[] readOneChannel(int channel, ByteOrder order) {
        int len = eegData.length / (getBinarySize() * channelCnt);
        double[] ret;
        double[] channelSet = new double[channelCnt];
        double resolution = info.getChannels().get(channel-1).getResolution();
        ret = new double[len];
        if (dataOrientation == DataOrientation.MULTIPLEXED) {
            for (int i = 0; i < len; i++) {
                channelSet = readChannelSet(i, channelSet, order);
                ret[i] = (channelSet[channel - 1] * resolution);
            }
        }
        if (dataOrientation == DataOrientation.VECTORIZED) {
                ret = readVectorizedChannel(channel - 1, ret, order);
        }

        return ret;
    }

    private double[] readVectorizedChannel(int channel, double[] ret, ByteOrder order) {
        int index = channel * getBinarySize() * ret.length;
        for (int i = 0; i < ret.length; i++) {
            if (getBinarySize() == 2) {
                ret[i] = readShort(index, order);
                index = index + 2;
            } else {
                ret[i] = readFloat(index, order);
                index = index + 4;
            }

        }
        return ret;
    }

    private double[] readChannelSet(int dataPos, double[] channelSet, ByteOrder order) {
        int index = dataPos * getBinarySize() * channelCnt;
        for (int i = 0; i < channelCnt; i++) {
            if (getBinarySize() == 2) {
               // System.out.println("EEG");
//                ByteBuffer bb = ByteBuffer.allocate(2);
//                bb.order(ByteOrder.LITTLE_ENDIAN);
//                bb.put(eegData[index]);
//                bb.put(eegData[index + 1]);
                channelSet[i] = readShort(index, order);
               // channelSet[i] = ((eegData[index] & 0xff) | (eegData[index+1] << 8)) << 16 >> 16;
                index = index + 2;
            }
            else {
               // System.out.println("AVG");
//                ByteBuffer bb = ByteBuffer.allocate(4);
//                bb.order(ByteOrder.LITTLE_ENDIAN);
//                bb.put(eegData[index]);
//                bb.put(eegData[index + 1]);
//                bb.put(eegData[index + 2]);
//                bb.put(eegData[index + 3]);
                channelSet[i] = readFloat(index, order);
                index = index + 4;
            }
        }
        return channelSet;
    }
    private short readShort(int index, ByteOrder order) {
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(order);
        bb.put(eegData[index]);
        bb.put(eegData[index + 1]);
        return bb.getShort(0);

    }

    private float readFloat(int index, ByteOrder order) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(order);
        bb.put(eegData[index]);
        bb.put(eegData[index + 1]);
        bb.put(eegData[index + 2]);
        bb.put(eegData[index + 3]);
        return bb.getFloat(0);

    }

    private int getBinarySize() {
        if (binaryType.equals(Float.class)) {
            return 4;
        }

        return 2; //default int16
    }
    
}
