package dkmeans.kmeans;

import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiachiliu on 11/30/14.
 */
public class KMeansLocalMapReduce {
    public static class KMeansLocalMapper extends Mapper<Object, Text, IntWritable, Text> {

        private static final int N = 15;

        @Override
        protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            List<KMVector> data = readData(context);
            LocalKMeans clf = new LocalKMeans(N);
            if (value.toString().trim().length() > 0) {
                int k = Integer.parseInt(value.toString().trim());
                clf.fit(data, k);
                List<KMVector> centers = clf.getCentroids();
                for (KMVector v : centers) {
                    context.write(new IntWritable(k), new Text(v.toString()));
                }
            }
        }

        private List<KMVector> readData(Context context) throws IOException {
            Path[] uris = DistributedCache.getLocalCacheFiles(context
                    .getConfiguration());

            BufferedReader br = new BufferedReader(new FileReader(uris[0].toString()));
            String line;
            List<KMVector> data = new ArrayList<KMVector>();
            while ((line = br.readLine()) != null) {
                KMVector row = DenseVector.fromString(line, N);
                data.add(row);
            }
            return data;
        }
    }

    public static class KMeansLocalReducer extends Reducer<IntWritable, Text, IntWritable, Text> {
        @Override
        protected void reduce(IntWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            for (Text t : values) {
                context.write(key, t);
            }
        }
    }
}
