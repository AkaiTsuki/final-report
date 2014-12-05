package dkmeans.kmeans;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.NLineInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by jiachiliu on 11/17/14.
 * A Controller class that handle the workflow for Distributed KMeans Algorithm
 */
public class KMeansController {

    private FileReadWriteUtil fileUtil;
    public static final String PROP_CURRENT_CENTROIDS = "currentCentroids";
    public static final String PROP_NEW_CENTROIDS = "newCentroids";
    public static final String PROP_NUM_OF_CLUSTER = "numOfClusters";
    public static final String PROP_NUM_OF_DIMENSION = "numOfDimensions";

    public KMeansController() {
        fileUtil = new FileReadWriteUtil();
    }

    public int train(String[] args) throws Exception {
        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args)
                .getRemainingArgs();

        if (otherArgs.length != 4) {
            System.err.println("Usage: KMeans <Input> <Output> <numOfClusters> <Vector Dimension>");
            System.exit(2);
        }

        // Data set file
        String dataset = otherArgs[0];
        // The folder that contains all current centroids
        String currentCentroids = otherArgs[1] + "/currentCentroids/";
        // The folder that contains all updated centroids
        String newCentroids = otherArgs[1] + "/newCentroids/";
        // Number of clusters
        int K = Integer.parseInt(otherArgs[2]);
        // Dimensions of vector
        int D = Integer.parseInt(otherArgs[3]);

        conf.set(PROP_CURRENT_CENTROIDS, currentCentroids);
        conf.set(PROP_NEW_CENTROIDS, newCentroids);
        conf.set(PROP_NUM_OF_CLUSTER, otherArgs[2]);
        conf.set(PROP_NUM_OF_DIMENSION, otherArgs[3]);

        // Write init centroids to new centroids folder
        fileUtil.write(randomCentroidsFromDatapoints(K, dataset, conf), conf, newCentroids);
        int loop = 0;
        do {
            // copy new centroids to current centroids so that mapreduce job can use
            // it generate new centroids
            fileUtil.copy(newCentroids, currentCentroids, conf, true);
            // run KMeans map reduce job for a single iteration
            runMapReduce(conf, dataset, newCentroids, K);
            loop++;
        } while (!isConverge(currentCentroids, newCentroids, conf, D));

        System.out.println("Total iterations: " + loop);
        return 1;
    }

    private List<String> randomCentroidsFromDatapoints(int K, String dataFile, Configuration config) throws Exception {
        List<String> centroids = new LinkedList<String>();

        FileSystem fileSystem = FileSystem.get(URI.create(dataFile), config);
        FSDataInputStream in = fileSystem.open(new Path(dataFile));
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;
        while ((line = br.readLine()) != null && centroids.size() < K) {
            centroids.add(line);
        }

        return centroids;
    }

    private boolean isConverge(String currentCentroid, String newCentroid, Configuration config, int N) throws Exception {
        List<String> currents = fileUtil.read(currentCentroid, config);
        List<String> news = fileUtil.read(newCentroid, config);

        double distance = 0.0;
        for (int i = 0; i < currents.size(); i++) {
            KMVector c = DenseVector.fromString(currents.get(i), N);
            KMVector n = DenseVector.fromString(news.get(i), N);
            distance += c.distance(n);
        }
        return distance < 10;
    }

    private int runMapReduce(Configuration conf, String input, String output, int numOfReducers)
            throws InterruptedException, IOException, ClassNotFoundException {
        conf.setInt(NLineInputFormat.LINES_PER_MAP, 1000);
        Job job = new Job(conf, "KMeansMapReduce");
        job.setInputFormatClass(NLineInputFormat.class);
        job.setJarByClass(KMeansMapReduce.class);
        job.setMapperClass(KMeansMapReduce.KMeansMapper.class);
        job.setReducerClass(KMeansMapReduce.KMeansReducer.class);
        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(NullWritable.class);
        job.setNumReduceTasks(numOfReducers);
        FileInputFormat.addInputPath(job, new Path(input));
        FileOutputFormat.setOutputPath(job, new Path(output));
        return job.waitForCompletion(true) ? 0 : 1;
    }
}
