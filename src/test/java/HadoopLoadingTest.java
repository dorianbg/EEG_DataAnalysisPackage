import EEGLoader.DataTransformer;
import EEGLoader.EEGDataTransformer;
import Utils.Const;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.Test;

import java.net.URI;
import java.nio.ByteOrder;
import java.util.ArrayList;

/**
 * @author Dorian Beganovic
 */
public class HadoopLoadingTest {

    @Test
    public void tryRAWEEG() {
        try {
            // instantiate the EEG data transformer
            DataTransformer transformer = new EEGDataTransformer();

            // variables which are important to track
            String hdfsvhdrFileLocation;
            String hdfsEEGFileLocation;
            String outputFileLocation;
            int channel;

            /* analyze the input
             we expect 4 inputs:
             - 1. location of .vhdr file
             - 2. location of .eeg file
             - 3. location where you want the output file to be stored
             - 4. number of channels
            */
            hdfsvhdrFileLocation = "/user/digitalAssistanceSystem/Datasets/University_Hospital_Pilsen/2_2_2017/male_2_11_1960_respiration_failure/ARO_2_2_instruction_nosound_01.vhdr";
            hdfsEEGFileLocation = "/user/digitalAssistanceSystem/Datasets/University_Hospital_Pilsen/2_2_2017/male_2_11_1960_respiration_failure/ARO_2_2_instruction_nosound_01.eeg";
            outputFileLocation = "/user/digitalAssistanceSystem/University_Hospital_Pilsen";
            channel = 3; // kinda the default value
            //List<ChannelInfo> channels = transformer.getChannelInfo(args[0]);
            //int channel = Integer.parseInt(args[args.length - 1]);

            // these are the extracted values from the .eeg file
            double[] dataInValues;
            dataInValues = transformer.readBinaryData(hdfsvhdrFileLocation, hdfsEEGFileLocation, channel, ByteOrder.LITTLE_ENDIAN);

            // SPARK PART

            SparkConf conf = new SparkConf().setMaster("local").setAppName("Work Count App");
            // Create a Java version of the Spark Context from the configuration
            JavaSparkContext sc = new JavaSparkContext(conf);


            // this is kinda ugly to do, but I don't think it's a big performance issue since
            // the arraylist doesn't have to regrow it should be an O(n) operation
            ArrayList<Double> dataIn = new ArrayList<>(dataInValues.length);
            for (double dataInValue : dataInValues) {
                dataIn.add(dataInValue);
            }
            JavaRDD<Double> rawVals = sc.parallelize(dataIn);

            // now write the output to FS

            FileSystem fs = FileSystem.get(URI.create(Const.HDFS_URI), Const.HDFS_CONF);

            if (fs.exists(new Path(outputFileLocation))){
                fs.delete(new Path(outputFileLocation),true);
            }
            // now save it using spark
            //System.out.println(fs.getUri());
            rawVals.saveAsTextFile(fs.getUri() + outputFileLocation);


            assert fs.exists(new Path(fs.getUri() + outputFileLocation)) == true;
//            for (EEGMarker marker: list) {
//                System.out.println(marker.getName() + " " + marker.getPosition());
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
