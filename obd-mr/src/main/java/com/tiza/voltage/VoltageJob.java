package com.tiza.voltage;

import com.tiza.hbase.filter.IntTimestampSuffixFilter;
import com.tiza.support.util.DateUtil;
import com.tiza.support.util.JacksonUtil;
import com.tiza.voltage.mr.VoltageMapper;
import com.tiza.voltage.mr.VoltageReducer;
import org.apache.commons.cli.*;
import org.apache.commons.lang.time.DateUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

/**
 * Description: VoltageJob
 * Author: DIYILIU
 * Update: 2018-09-03 16:35
 */
public class VoltageJob {

    public static void main(String[] args) throws Exception {
        System.out.println(JacksonUtil.toJson(args));

        Options options = new Options();
        options.addOption("d", "day", true, "analysis time");
        Option accOp = new Option("a", "acc", true, "acc status[0: off,1: on]");
        accOp.setRequired(true);
        options.addOption(accOp);

        CommandLineParser parser = new PosixParser();
        CommandLine cli = parser.parse(options, args);

        // 分析时间
        String day = DateUtil.getYMD(Calendar.getInstance()) + "";
        if (cli.hasOption("day")) {
            day = cli.getOptionValue("day");
        }
        Date end = DateUtil.stringToDate(day, "yyyyMMdd");
        Date begin = DateUtils.addDays(end, -1);

        String acc = cli.getOptionValue("acc");
        // 参数异常
        if (end == null || !(acc.equals("0") || acc.equals("1"))) {
            System.out.println("args error!");
            return;
        }

        Properties config = new Properties();
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties")) {
            config.load(in);

            Configuration conf = HBaseConfiguration.create();
            conf.set("hbase.zookeeper.quorum", config.getProperty("hbase.zk-quorum"));
            conf.set("hbase.zookeeper.property.clientPort", config.getProperty("hbase.zk-port"));
            conf.set("acc", acc);
            conf.set("day", String.format("%1$tY%1$tm%1$td", begin));

            // 电压数据源
            String dataTable = config.getProperty("hbase.obd-table");

            // HBase scan
            Scan scan = new Scan();
            Filter rowFilter = new IntTimestampSuffixFilter(begin.getTime(), end.getTime());
            FilterList qualifierFilters = new FilterList(FilterList.Operator.MUST_PASS_ALL);
            qualifierFilters.addFilter(rowFilter);

            // 熄火电压
            if (acc.equals("0")) {
                Filter accFilter = new SingleColumnValueFilter(Bytes.toBytes("j"), Bytes.toBytes("acc"), CompareFilter.CompareOp.EQUAL, Bytes.toBytes("0"));
                qualifierFilters.addFilter(accFilter);

                dataTable = config.getProperty("hbase.gps-table");
            }
            scan.setFilter(qualifierFilters);

            Job job = Job.getInstance(conf);
            job.setJobName("DailyVoltage");
            job.setJarByClass(VoltageJob.class);

            // map
            job.setMapperClass(VoltageMapper.class);
            job.setInputFormatClass(TableInputFormat.class);
            TableMapReduceUtil.initTableMapperJob(dataTable, scan, VoltageMapper.class, IntWritable.class, Text.class, job);

            // reduce
            job.setReducerClass(VoltageReducer.class);
            job.setOutputFormatClass(NullOutputFormat.class);
            TableMapReduceUtil.initTableReducerJob(config.getProperty("hbase.out-table"), VoltageReducer.class, job);

            System.exit(job.waitForCompletion(true) ? 0 : 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
