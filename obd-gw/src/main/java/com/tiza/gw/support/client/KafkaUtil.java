package com.tiza.gw.support.client;

import com.tiza.gw.support.model.KafkaMsg;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Description: KafkaUtil
 * Author: DIYILIU
 * Update: 2018-06-20 16:57
 */

@Slf4j
public class KafkaUtil implements Runnable {
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private final Queue<KafkaMsg> dataPool = new ConcurrentLinkedQueue();

    private String topic;
    private Producer producer;

    public KafkaUtil(String topic, Producer producer) {
        this.topic = topic;
        this.producer = producer;

        executorService.scheduleAtFixedRate(this, 10, 3, TimeUnit.SECONDS);
    }

    public void send(KafkaMsg msg) {
        dataPool.add(msg);
    }

    @Override
    public void run() {
        while (!dataPool.isEmpty()) {
            KafkaMsg data = dataPool.poll();

            producer.send(new KeyedMessage(topic, data.getKey(), data.getValue()));
        }
    }
}
