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
 * Read the original file, output all songs with its ids that appear in the file
 */
public class DistributedSongListCreator {
    public static class SongListMapper extends Mapper<Object, Text, Text, NullWritable> {
        private static Text dummy = new Text("d");
        private Text songname = new Text();

        @Override
        protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            if (line.length() > 0) {
                String[] tokens = line.split("\t");
                songname.set(tokens[1]);
                context.write(songname, NullWritable.get());
            }
        }
    }

    public static class SongListReducer extends Reducer<Text, NullWritable, Text, IntWritable> {
        private IntWritable sid = new IntWritable();
        private Text song = new Text();
        private int counter = 0;

        @Override
        protected void reduce(Text key, Iterable<NullWritable> values, Context context) throws IOException, InterruptedException {
            String songname = key.toString();
            song.set(songname);
            sid.set(counter);
            context.write(song, sid);
            counter++;
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args)
                .getRemainingArgs();
        if (otherArgs.length != 2) {
            System.err.println("Usage: DistributedSongListGenerator <in> <out>");
            System.exit(2);
        }
        Job job = new Job(conf, "DistributedSongListGenerator");
        job.setJarByClass(DistributedSongListCreator.class);
        job.setMapperClass(SongListMapper.class);
        job.setReducerClass(SongListReducer.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(NullWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
