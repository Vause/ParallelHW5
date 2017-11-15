import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class WordCount 
{

    public static class TokenizerMapper
    extends Mapper <Object, Text, Text, IntWritable> 
	{

        public void map(Object key, Text value, Context context)
        throws IOException,
        InterruptedException 
		{
            Map<String, Integer> map = new HashMap<String, Integer> ();
            StringTokenizer tokenizer = new StringTokenizer(value.toString());

            while (tokenizer.hasMoreTokens()) 
			{
                String lineToken = tokenizer.nextToken();
                if (map.containsKey(lineToken)) 
				{
                    int sum = map.get(lineToken) + 1;
                    map.put(lineToken, sum);
                } 
				else 
				{
                    map.put(lineToken, 1);
                }
            }

            for(Map.Entry<String, Integer> entry : map.entrySet())
			{
                String stringKey = entry.getKey();
                int total = entry.getValue().intValue();
                context.write(new Text(stringKey), new IntWritable(total));
			}
        }
    }

    public static class IntSumReducer
    extends Reducer < Text, IntWritable, Text, IntWritable > 
	{
        private IntWritable result = new IntWritable();

        public void reduce(Text key, Iterable < IntWritable > values,
            Context context
        ) throws IOException,
        InterruptedException 
		{
            int sum = 0;
            for (IntWritable val: values) 
			{
                sum += val.get();
            }
            result.set(sum);
            context.write(key, result);
        }
    }

    public static void main(String[] args) throws Exception 
	{
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "word count");
        job.setJarByClass(WordCount.class);
        job.setMapperClass(TokenizerMapper.class);
        //job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(IntSumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}