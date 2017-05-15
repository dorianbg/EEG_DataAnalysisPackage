package EEGLoader.test;


import EEGLoader.signal.ChannelInfo;
import EEGLoader.signal.DataTransformer;
import EEGLoader.signal.EEGDataTransformer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.List;

/**
 * This class shows an example how to use the EEGLoader.
 *
 * Tato t��da ukazuje p��klad, jak pou��t EEGLoader.
 *
 * @author Jan Stebetak
 */
public class Main {


    public static void main(String[] args) {
        try {
            DataTransformer transformer = new EEGDataTransformer();
            //List<EEGMarker> list = transformer.readMarkerList("c:\\java\\guess_the_number\\data\\numbers\\Blatnice\\blatnice20141023_9.vmrk");
            List<ChannelInfo> channels = transformer.getChannelInfo(args[0]);
            int channel = Integer.parseInt(args[args.length - 1]);
            double[] dataInValues;
            if (args.length == 3) {
                dataInValues = transformer.readBinaryData(args[0], args[1], channel, ByteOrder.LITTLE_ENDIAN);
            } else {
                dataInValues = transformer.readBinaryData(args[0], channel, ByteOrder.LITTLE_ENDIAN);
            }


            write("/Users/dorianbeganovic/Desktop/hdfs2.txt",dataInValues);
            for (double value : dataInValues) {
                System.out.println(value);
            }


//            for (EEGMarker marker: list) {
//                System.out.println(marker.getName() + " " + marker.getPosition());
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void write (String filename, double[]x) throws IOException {
        BufferedWriter outputWriter = null;
        outputWriter = new BufferedWriter(new FileWriter(filename));
        for (int i = 0; i < x.length; i++) {
            // Maybe:
            outputWriter.write(x[i]+"");
            outputWriter.newLine();
        }
        outputWriter.flush();
        outputWriter.close();
    }

}
