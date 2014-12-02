package dkmeans.preprocess;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
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
 * This job will generate a list of users with generated ids.
 */
public class DistributedUserListCreator {
    public static class UserListMapper extends Mapper<Object, Text, Text, NullWritable> {
        private static Text dummy = new Text("d");
        private Text username = new Text();

        @Override
        protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            if (line.length() > 0) {
                String[] tokens = line.split("\t");
                username.set(tokens[0]);
                context.write(username, NullWritable.get());
            }
        }
    }

    public static class UserListReducer extends Reducer<Text, NullWritable, Text, IntWritable> {
        private IntWritable uid = new IntWritable();
        private Text user = new Text();
        private int counter = 0;

        @Override
        protected void reduce(Text key, Iterable<NullWritable> values, Context context) throws IOException, InterruptedException {
            String username = key.toString();
            user.set(username);
            uid.set(counter);
            context.write(user, uid);
            counter++;
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args)
                .getRemainingArgs();
        if (otherArgs.length != 2) {
            System.err.println("Usage: DistributedUserListGenerator <in> <out>");
            System.exit(2);
        }
        Job job = new Job(conf, "DistributedUserListGenerator");
        job.setJarByClass(DistributedUserListCreator.class);
        job.setMapperClass(UserListMapper.class);
        job.setReducerClass(UserListReducer.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(NullWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
