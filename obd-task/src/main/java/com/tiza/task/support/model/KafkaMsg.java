package com.tiza.task.support.model;

import lombok.Data;

/**
 * Description: KafkaMsg
 * Author: DIYILIU
 * Update: 2018-08-08 11:30
 */

@Data
public class KafkaMsg {
    private String topic;

    private String key;

    private String value;

    public KafkaMsg() {
    }

    public KafkaMsg(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public KafkaMsg(String topic, String key, String value) {
        this.topic = topic;
        this.key = key;
        this.value = value;
    }
}
