package com.tiza.das.config;

import com.diyiliu.plugin.cache.ICache;
import com.diyiliu.plugin.cache.ram.RamCacheProvider;
import com.tiza.das.support.util.KafkaUtil;
import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;
import kafka.serializer.StringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Description: DasConfig
 * Author: DIYILIU
 * Update: 2018-07-30 10:11
 */

@Configuration
@EnableScheduling
@PropertySource(value = {"classpath:config.properties"})
public class DasConfig {

    @Resource
    private Environment environment;

    @Bean
    public KafkaUtil kafkaUtil(){
        Map kafkaProp = new HashMap();
        // 消息传递到broker时的序列化方式
        kafkaProp.put("serializer.class", StringEncoder.class.getName());

        // 是否获取反馈
        // 0是不获取反馈(消息有可能传输失败)
        // 1是获取消息传递给leader后反馈(其他副本有可能接受消息失败)
        // -1是所有in-sync replicas接受到消息时的反馈
        kafkaProp.put("request.required.acks", "1");

        // 内部发送数据是异步还是同步 sync：同步, 默认 async：异步
        kafkaProp.put("producer.type", "async");
        // 重试次数
        kafkaProp.put("message.send.max.retries", "3");
        // 异步提交的时候(async)，并发提交的记录数
        kafkaProp.put("batch.num.messages", "200");
        // 设置缓冲区大小，默认10KB
        kafkaProp.put("send.buffer.bytes", "102400");

        String brokerList = environment.getProperty("kafka.broker-list");
        String topic =  environment.getProperty("kafka.push-topic");

        Properties props = new Properties();
        // kafka broker 列表
        props.put("metadata.broker.list", brokerList);
        props.putAll(kafkaProp);
        Producer<String, String> producer =  new Producer(new ProducerConfig(props));

        return new KafkaUtil(topic, producer);
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
}
