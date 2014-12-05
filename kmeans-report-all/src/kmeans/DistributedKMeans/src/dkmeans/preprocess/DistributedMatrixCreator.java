package dkmeans.preprocess;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import java.io.IOException;

/**
 * Created by jiachiliu on 11/16/14.
 * Read the matrix input file which each row in the file represents a cell
 * Convert the file to representation that each row in the file is a matrix row
 */
public class DistributedMatrixCreator {
    public static class MatrixCreatorMapper extends Mapper<Object, Text, IntWritable, Text> {
        private IntWritable userid = new IntWritable();
        private Text history = new Text();

        @Override
        protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            if (line.length() > 0) {
                String[] tokens = line.split("\t");
                userid.set(Integer.parseInt(tokens[0]));
                history.set(tokens[1] + ":" + tokens[2]);
                context.write(userid, history);
            }
        }
    }

    public static class MatrixCreatorReducer extends Reducer<IntWritable, Text, IntWritable, Text> {
        private IntWritable userid = new IntWritable();
        private Text history = new Text();

        @Override
        protected void reduce(IntWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            int user = key.get();
            StringBuilder sb = new StringBuilder();
            userid.set(user);
            for (Text v : values) {
                String col = v.toString();
                if (sb.length() > 0) sb.append(",");
                sb.append(col);
            }
            history.set(sb.toString());
            context.write(userid, history);
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args)
                .getRemainingArgs();
        if (otherArgs.length != 2) {
            System.err.println("Usage: DistributedMatrixCreator <in> <out>");
            System.exit(2);
        }
        Job job = new Job(conf, "DistributedMatrixCreator");
        job.setJarByClass(DistributedMatrixCreator.class);
        job.setMapperClass(MatrixCreatorMapper.class);
        job.setReducerClass(MatrixCreatorReducer.class);
        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
