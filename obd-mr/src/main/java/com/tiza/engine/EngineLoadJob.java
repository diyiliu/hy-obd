package com.tiza.engine;

import com.tiza.engine.mr.LoadMapper;
import com.tiza.engine.mr.LoadReducer;
import com.tiza.hbase.filter.IntTimestampSuffixFilter;
import com.tiza.support.util.DateUtil;
import com.tiza.support.util.JacksonUtil;
import org.apache.commons.cli.*;
import org.apache.commons.lang.time.DateUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
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
 * Description: EngineLoadJob
 * Author: DIYILIU
 * Update: 2018-09-06 10:03
 */
public class EngineLoadJob {


    public static void main(String[] args) throws Exception {
        System.out.println(JacksonUtil.toJson(args));
        CommandLine cli = initCli(args);

        // 分析时间
        String day = DateUtil.getYMD(Calendar.getInstance()) + "";
        if (cli.hasOption("day")) {
            day = cli.getOptionValue("day");
        }
        Date end = DateUtil.stringToDate(day, "yyyyMMdd");
        Date begin = DateUtils.addDays(end, -1);

        // 参数异常
        if (end == null) {
            System.out.println("args error!");
            return;
        }

        Properties config = new Properties();
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties")) {
            config.load(in);

            Configuration conf = HBaseConfiguration.create();
            conf.set("hbase.zookeeper.quorum", config.getProperty("hbase.zk-quorum"));
            conf.set("hbase.zookeeper.property.clientPort", config.getProperty("hbase.zk-port"));
            conf.set("day", String.format("%1$tY%1$tm%1$td", begin));

            // 数据源
            String dataTable = config.getProperty("hbase.obd-table");

            // HBase scan
            Scan scan = new Scan();
            Filter rowFilter = new IntTimestampSuffixFilter(begin.getTime(), end.getTime());
            scan.setFilter(rowFilter);

            Job job = Job.getInstance(conf);
            job.setJobName("EngineLoadJob");
            job.setJarByClass(EngineLoadJob.class);

            // map
            job.setMapperClass(LoadMapper.class);
            job.setInputFormatClass(TableInputFormat.class);
            TableMapReduceUtil.initTableMapperJob(dataTable, scan, LoadMapper.class, IntWritable.class, Text.class, job);

            // reduce
            job.setReducerClass(LoadReducer.class);
            job.setOutputFormatClass(NullOutputFormat.class);
            TableMapReduceUtil.initTableReducerJob(config.getProperty("hbase.out-table"), LoadReducer.class, job);

            System.exit(job.waitForCompletion(true) ? 0 : 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static CommandLine initCli(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption("d", "day", true, "analysis time");

        CommandLineParser parser = new PosixParser();
        return parser.parse(options, args);
    }
}
