package com.tiza.task.support.util;

import com.tiza.task.support.model.KafkaMsg;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * Description: KafkaUtil
 * Author: DIYILIU
 * Update: 2018-06-20 16:57
 */

@Slf4j
public class KafkaUtil {

    private Producer producer;

    public KafkaUtil(Producer producer) {
        this.producer = producer;
    }

    public void send(KafkaMsg msg) {
        producer.send(new KeyedMessage(msg.getTopic(), msg.getKey(), msg.getValue()));
    }

    public Producer getProducer() {
        return producer;
    }
}
