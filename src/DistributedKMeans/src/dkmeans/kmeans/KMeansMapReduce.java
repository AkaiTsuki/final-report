package dkmeans.kmeans;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jiachiliu on 11/18/14.
 * MapReduce job for a single KMeans iteration
 */
public class KMeansMapReduce {

    public static class KMeansMapper extends Mapper<Object, Text, IntWritable, Text> {

        private List<KMVector> centroids = new LinkedList<KMVector>();
        private int N;
        private IntWritable keyOut = new IntWritable();
        private Text valueOut = new Text();

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            Configuration config = context.getConfiguration();
            N = Integer.parseInt(config.get(KMeansController.PROP_NUM_OF_DIMENSION));
            String currentCentroid = config.get(KMeansController.PROP_CURRENT_CENTROIDS);
            FileReadWriteUtil util = new FileReadWriteUtil();
            try {
                List<String> contents = util.read(currentCentroid, config);
                for (String c : contents) {
                    centroids.add(DenseVector.fromString(c, N));
                }
            } catch (Exception e) {
                throw new InterruptedException(e.getMessage());
            }
        }

        @Override
        protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            double min = Double.MAX_VALUE;
            int cluster = 0;
        
            String point = value.toString().trim();
            if (point.length() > 0) {
                KMVector p = DenseVector.fromString(point, N);
                for (int i = 0; i < centroids.size(); i++) {
                    double d = p.distance(centroids.get(i));
                    if (d < min) {
                        min = d;
                        cluster = i;
                    }
                }
                keyOut.set(cluster);
                valueOut.set(point);
                context.write(keyOut, valueOut);
            }
        }
    }

    public static class KMeansReducer extends Reducer<IntWritable, Text, Text, NullWritable> {
        private int N;
        private Text keyOut = new Text();
        private static NullWritable valueOut = NullWritable.get();

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            Configuration config = context.getConfiguration();
            N = Integer.parseInt(config.get(KMeansController.PROP_NUM_OF_DIMENSION));
        }

        @Override
        protected void reduce(IntWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            KMVector centroid = new DenseVector(N);

            int count = 0;
            for (Text t : values) {
                KMVector p = DenseVector.fromString(t.toString(), N);
                for (int i = 0; i < N; i++) {
                    double sum = centroid.get(i) + p.get(i);
                    centroid.set(i, sum);
                }
                count++;
            }

            for (int i = 0; i < N; i++) {
                centroid.set(i, (centroid.get(i) / count));
            }

            keyOut.set(centroid.toString());
            context.write(keyOut, valueOut);
        }
    }
}
