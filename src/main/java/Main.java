/**
 * @author Dorian Beganovic
 */
public class Main {
    public static void main(String[] args) {
        System.out.println();
    }
}


















/*
below is only "throwaway" temp code
 */



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
