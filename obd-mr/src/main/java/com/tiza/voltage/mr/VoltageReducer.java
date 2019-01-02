package com.tiza.voltage.mr;

import com.tiza.support.util.JacksonUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description: VoltageReducer
 * Author: DIYILIU
 * Update: 2018-09-04 09:38
 */
public class VoltageReducer extends TableReducer<IntWritable, Text, NullWritable> {

    private int acc;

    private String day;

    @Override
    protected void setup(Context context) {
        Configuration conf = context.getConfiguration();
        acc = conf.getInt("acc", 1);
        day = conf.get("day");

        System.out.println("reduce setup...[" + day + ", " + acc + "]");
    }

    @Override
    protected void cleanup(Context context) {

    }

    @Override
    protected void reduce(IntWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        int vehicleId = key.get();

        Map<String, AtomicInteger> map = new HashMap();
        for (Text t : values) {
            String value = t.toString();
            if (map.containsKey(value)) {
                map.get(value).incrementAndGet();
            } else {
                map.put(value, new AtomicInteger(1));
            }
        }

        if (MapUtils.isNotEmpty(map)) {
            String json = JacksonUtil.toJson(map);
            System.out.println("电压[acc=" + acc + "]统计结果" + vehicleId + ":" + json);

            // 主键
            byte[] rowKey = Bytes.add(Bytes.toBytes(vehicleId), Bytes.toBytes(Integer.valueOf(day)), Bytes.toBytes("voltage:" + (acc == 1 ? "on" : "off")));
            Put put = new Put(rowKey);
            put.add(Bytes.toBytes("j"), Bytes.toBytes("v"), Bytes.toBytes(json));

            context.write(NullWritable.get(), put);
        }
    }
}
