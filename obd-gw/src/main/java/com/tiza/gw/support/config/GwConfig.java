package com.tiza.gw.support.config;

import com.diyiliu.plugin.cache.ICache;
import com.diyiliu.plugin.cache.ram.RamCacheProvider;
import com.diyiliu.plugin.util.SpringUtil;
import com.tiza.gw.netty.server.ObdServer;
import com.tiza.gw.support.client.KafkaUtil;
import com.tiza.gw.support.client.RedisClient;
import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;
import kafka.serializer.StringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Description: GwConfig
 * Author: DIYILIU
 * Update: 2018-06-27 14:13
 */

@Configuration
@EnableScheduling
public class GwConfig {

    @Resource
    private Environment environment;

    @Resource
    private RedisTemplate redisTemplate;

    @Bean
    public RedisClient redisClient(){
        RedisClient redisClient = new RedisClient();
        redisClient.setRedisTemplate(redisTemplate);

        return redisClient;
    }

    private Map kafkaProp = new HashMap(){
        {
            // 消息传递到broker时的序列化方式
            this.put("serializer.class", StringEncoder.class.getName());

            // 是否获取反馈
            // 0是不获取反馈(消息有可能传输失败)
            // 1是获取消息传递给leader后反馈(其他副本有可能接受消息失败)
            // -1是所有in-sync replicas接受到消息时的反馈
            this.put("request.required.acks", "1");

            // 内部发送数据是异步还是同步 sync：同步, 默认 async：异步
            this.put("producer.type", "async");
            // 重试次数
            this.put("message.send.max.retries", "3");
            // 异步提交的时候(async)，并发提交的记录数
            this.put("batch.num.messages", "200");
            // 设置缓冲区大小，默认10KB
            this.put("send.buffer.bytes", "102400");
        }
    };

    @Bean
    public KafkaUtil kafkaUtil(){
        String brokerList = environment.getProperty("kafka.broker-list");
        String topic =  environment.getProperty("kafka.raw-topic");

        Properties props = new Properties();
        // kafka broker 列表
        props.put("metadata.broker.list", brokerList);
        props.putAll(kafkaProp);
        Producer<String, String> producer =  new Producer(new ProducerConfig(props));

        return new KafkaUtil(topic, producer);
    }


    @Bean
    public ObdServer obdServer() {
        ObdServer obdServer = new ObdServer();
        obdServer.setPort(environment.getProperty("obd.gw-port", Integer.class));
        obdServer.init();

        return obdServer;
    }

    /*
    @Bean
    public MsgSenderTask msgSenderTask() {
        MsgSenderTask senderTask = new MsgSenderTask();
        senderTask.setKafkaUtil(kafkaUtil());
        senderTask.setOnlineCacheProvider(onlineCacheProvider());
        senderTask.execute();

        return senderTask;
    }

    @Bean
    public KeepAliveTask keepAliveTask() {
        KeepAliveTask aliveTask = new KeepAliveTask();
        aliveTask.setOnlineCacheProvider(onlineCacheProvider());
        aliveTask.execute();

        return aliveTask;
    }

    @Bean
    public CMDInitializer cmdInitializer(){
        CMDInitializer cmdInitializer = new CMDInitializer();
        cmdInitializer.setProtocols(new ArrayList(){
            {
                this.add(ObdDataProcess.class);
            }
        });

        return cmdInitializer;
    }
    */

    /**
     * spring 工具类
     *
     * @return
     */
    @Bean
    public SpringUtil springUtil() {

        return new SpringUtil();
    }

    /**
     * 指令缓存
     *
     * @return
     */
    @Bean
    public ICache cmdCacheProvider() {

        return new RamCacheProvider();
    }

    /**
     * 车辆缓存
     *
     * @return
     */
    @Bean
    public ICache vehicleCacheProvider() {

        return new RamCacheProvider();
    }

    /**
     * 设备在线
     *
     * @return
     */
    @Bean
    public ICache onlineCacheProvider() {

        return new RamCacheProvider();
    }
}
