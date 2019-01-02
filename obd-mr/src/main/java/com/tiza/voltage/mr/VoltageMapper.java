package com.tiza.voltage.mr;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;

import java.io.IOException;

/**
 * Description: VoltageMapper
 * Author: DIYILIU
 * Update: 2018-09-03 16:37
 */

public class VoltageMapper extends TableMapper<IntWritable, Text> {

    private static final byte[] FAMILY = Bytes.toBytes("j");
    private static final byte[] QUALIFIER = Bytes.toBytes("voltage");

    @Override
    protected void setup(Context context) {

    }

    @Override
    protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException {
        int vehicleId = Bytes.toInt(key.get());
        if (value.containsColumn(FAMILY, QUALIFIER)) {

            String voltage = Bytes.toString(value.getValue(FAMILY, QUALIFIER));
            context.write(new IntWritable(vehicleId), new Text(String.format("%.1f", Double.valueOf(voltage))));
        }
    }
}
