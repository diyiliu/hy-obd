package com.tiza.engine.mr;

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
 * Description: LoadReducer
 * Author: DIYILIU
 * Update: 2018-09-06 10:07
 */
public class LoadReducer extends TableReducer<IntWritable, Text, NullWritable> {

    private String day;

    @Override
    protected void setup(Context context) {
        Configuration conf = context.getConfiguration();
        day = conf.get("day");

        System.out.println("reduce setup...[" + day + "]");
    }

    @Override
    protected void cleanup(Context context) {

    }

    @Override
    protected void reduce(IntWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        int vehicleId = key.get();

        Map<Integer, AtomicInteger> map = new HashMap();
        for (Text t : values) {
            String value = t.toString();
            int load = Integer.parseInt(value);
            if (load <= 0) {
                continue;
            }

            int l = (load / 5 + 1) * 5;
            if (map.containsKey(l)) {
                map.get(l).incrementAndGet();
            } else {
                map.put(l, new AtomicInteger(1));
            }
        }

        if (MapUtils.isNotEmpty(map)) {
            String json = JacksonUtil.toJson(map);
            System.out.println("发动机负载统计结果" + vehicleId + ":" + json);

            // 主键
            byte[] rowKey = Bytes.add(Bytes.toBytes(vehicleId), Bytes.toBytes(Integer.valueOf(day)), Bytes.toBytes("engineLoad"));
            Put put = new Put(rowKey);
            put.add(Bytes.toBytes("j"), Bytes.toBytes("v"), Bytes.toBytes(json));

            context.write(NullWritable.get(), put);
        }
    }
}
