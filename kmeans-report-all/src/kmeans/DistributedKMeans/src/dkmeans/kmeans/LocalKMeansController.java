package dkmeans.kmeans;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.NLineInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import java.net.URI;

/**
 * Created by jiachiliu on 11/30/14.
 */
public class LocalKMeansController {

    public int run(String[] args) throws Exception {
        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args)
                .getRemainingArgs();

        String dataPath = otherArgs[0];
        String configPath = otherArgs[1];
        String N = otherArgs[2];
        String outputPath = otherArgs[3];

        conf.setInt(NLineInputFormat.LINES_PER_MAP, 1);

        Job job = new Job(conf, "LocalKMeansController");
        job.setInputFormatClass(NLineInputFormat.class);
        job.setJarByClass(KMeansLocalMapReduce.class);
        job.setMapperClass(KMeansLocalMapReduce.KMeansLocalMapper.class);
        job.setReducerClass(KMeansLocalMapReduce.KMeansLocalReducer.class);
        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(NullWritable.class);
        job.setNumReduceTasks(6);


        DistributedCache.addCacheFile(new URI(dataPath),
                job.getConfiguration());

        FileInputFormat.addInputPath(job, new Path(configPath));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));
        return job.waitForCompletion(true) ? 0 : 1;
    }

//    private List<KMVector> readData(String path, int N) throws Exception {
//        BufferedReader br = new BufferedReader(new FileReader(path));
//        String line;
//        List<KMVector> data = new ArrayList<KMVector>();
//        while ((line = br.readLine()) != null) {
//            KMVector row = DenseVector.fromString(line, N);
//            data.add(row);
//        }
//        return data;
//    }

    public static void main(String[] args) throws Exception {
        LocalKMeansController c = new LocalKMeansController();
        c.run(args);
    }
}
