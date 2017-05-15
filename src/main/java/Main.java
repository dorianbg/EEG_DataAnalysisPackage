import EEGLoader.signal.ChannelInfo;
import EEGLoader.signal.DataTransformer;
import EEGLoader.signal.EEGDataTransformer;
import cz.zcu.kiv.eegdsp.common.ISignalProcessor;
import cz.zcu.kiv.eegdsp.wavelet.discrete.WaveletTransformationDiscrete;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.util.Progressable;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.io.*;
import java.net.URI;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.System.out;

/**
 * Created by dbg on 07/05/2017.
 */
public class Main {
    public static void main(String[] args) {
        //trySpark2();
        tryRAWEEG(args);
    }


    public static void tryRAWEEG(String[] args) {
        try {
            DataTransformer transformer = new EEGDataTransformer();

            String hdfsvhdrFileLocation = "/user/digitalAssistanceSystem/data/numbers/KVary/KArlovyVary_20150507_04.vhdr";
            String hdfsEEGFileLocation = "/user/digitalAssistanceSystem/data/numbers/KVary/KArlovyVary_20150507_04.eeg";
            String outputFileLocation = "hdfs://localhost:8020/user/digitalAssistanceSystem/processed_KArlovyVary_20150507";

            List<ChannelInfo> channels = transformer.getChannelInfo(args[0]);
            //int channel = Integer.parseInt(args[args.length - 1]);
            int channel = 3; // kinda the default value
            double[] dataInValues;
            dataInValues = transformer.readBinaryData(hdfsvhdrFileLocation, hdfsEEGFileLocation, channel, ByteOrder.LITTLE_ENDIAN);

//68.4, 69.3, 73

            SparkConf conf = new SparkConf().setMaster("local").setAppName("Work Count App");
            // Create a Java version of the Spark Context from the configuration
            JavaSparkContext sc = new JavaSparkContext(conf);
            JavaRDD<double[]> rawVals = sc.parallelize(Arrays.asList(dataInValues));
            rawVals.saveAsTextFile(args[args.length - 2]);

            // write("/Users/dorianbeganovic/Desktop/hdfs2.txt",dataInValues);

//            for (EEGMarker marker: list) {
//                System.out.println(marker.getName() + " " + marker.getPosition());
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    public static void trySpark() {
        SparkConf conf = new SparkConf().setMaster("local").setAppName("Work Count App");
        // Create a Java version of the Spark Context from the configuration
        JavaSparkContext sc = new JavaSparkContext(conf);

        JavaRDD<String> lines =
                sc.textFile("/Users/dorianbeganovic/gsoc/Spark_EEG/src/main/resources/split_set/tagged_testing_data/raw_epochs_O1.txt");

        lines
                .map((String x) -> x.trim().split("\\s+"))
                .map((String[] x) -> Arrays.stream(x).mapToDouble(Double::parseDouble).toArray())
                .foreach((double[] x) -> out.println("line length: " + x.length + ", content: " + Arrays.toString(x)));
    }

    public static void tryHadoop(){
        try{
            Configuration conf = new Configuration();
            conf.set("fs.defaultFS", "hdfs://147.228.63.46:8020/user/digitalAssistanceSystem/data");
            FileSystem fs = FileSystem.get(conf);


            Path pt = new Path("");

            out.println(fs.isDirectory(pt));
            /*
            BufferedReader br=new BufferedReader(new InputStreamReader(fs.open(pt)));

            String line;
            line=br.readLine();
            while (line != null){
                System.out.println(line);
                line=br.readLine();
            }

        }catch(Exception e){
            out.println("failed");
        }
    }

    public static void tryHadoop2() {
        String uri = "hdfs://147.228.63.46:8020/user/digitalAssistanceSystem/data/svmclassifier.txt";
        Configuration conf = new Configuration();
        FSDataInputStream in = null;
        try {
            FileSystem fs = FileSystem.get(URI.create(uri), conf);
            in = fs.open(new Path(uri));
            IOUtils.copyBytes(in, out, 4096, false); in.seek(0); // go back to the start of the file IOUtils.copyBytes(in, System.out, 4096, false);

        } catch (Exception e){
            e.printStackTrace();
            out.printf("Failed");
        }
        finally {
            IOUtils.closeStream(in);
        }
    }


    public static void tryHadoop3(){
        String uri = "hdfs://localhost:8020/user/digitalAssistanceSystem/data";
        Configuration conf = new Configuration();
        BufferedReader br = null;

        try {
            FileSystem fs = FileSystem.get(URI.create(uri), conf);

            Path[] paths = new Path[1];

            for (int i = 0; i < paths.length; i++) {
                paths[i] = new Path(uri);
            }
            FileStatus[] status = fs.listStatus(paths);
            Path[] listedPaths = FileUtil.stat2Paths(status);
            String filesList = "";
            for (Path p : listedPaths) {
                out.println(p);
                filesList += p.toString();
            }

            Path file = new Path("hdfs://localhost:8020/user/digitalAssistanceSystem/filesInDir.txt");
            if ( fs.exists( file )) { fs.delete( file, true ); }
            OutputStream os = fs.create( file);
            BufferedWriter bufferedWriter = new BufferedWriter( new OutputStreamWriter( os, "UTF-8" ) );
            bufferedWriter.write(filesList);
            bufferedWriter.close();
            fs.close();

        }
        catch (Exception e){
            out.println(e.getMessage());
        }
        finally {
        }
    }

        */


}
