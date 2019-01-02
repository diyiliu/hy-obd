package com.tiza.engine.mr;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;

import java.io.IOException;

/**
 * Description: LoadMapper
 * Author: DIYILIU
 * Update: 2018-09-06 10:07
 */
public class LoadMapper extends TableMapper<IntWritable, Text> {

    private static final byte[] FAMILY = Bytes.toBytes("j");
    private static final byte[] QUALIFIER = Bytes.toBytes("engineLoad");

    @Override
    protected void setup(Context context) {

    }

    @Override
    protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException {
        int vehicleId = Bytes.toInt(key.get());
        if (value.containsColumn(FAMILY, QUALIFIER)) {

            String load = Bytes.toString(value.getValue(FAMILY, QUALIFIER));
            if (StringUtils.isNotEmpty(load)){
                context.write(new IntWritable(vehicleId), new Text(load));
            }
        }
    }
}
