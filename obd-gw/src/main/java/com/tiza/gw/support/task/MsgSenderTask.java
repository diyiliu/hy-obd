package com.tiza.gw.support.task;

import com.diyiliu.plugin.cache.ICache;
import com.diyiliu.plugin.model.SendMsg;
import com.diyiliu.plugin.task.ITask;
import com.diyiliu.plugin.util.CommonUtil;
import com.diyiliu.plugin.util.JacksonUtil;
import com.tiza.gw.support.client.KafkaUtil;
import com.tiza.gw.support.model.KafkaMsg;
import com.tiza.gw.support.model.MsgPipeline;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Description: MsgSenderTask
 * Author: DIYILIU
 * Update: 2018-06-26 16:01
 */

@Slf4j
@Component
public class MsgSenderTask implements ITask {
    private final static Queue<SendMsg> msgPool = new ConcurrentLinkedQueue();

    @Resource
    private ICache onlineCacheProvider;

    @Resource
    private KafkaUtil kafkaUtilRaw;

    @Scheduled(fixedDelay = 1000, initialDelay = 3 * 1000)
    public void execute() {
        while (!msgPool.isEmpty()) {
            SendMsg msg = msgPool.poll();

            String device = msg.getDevice();
            int cmd = msg.getCmd();
            byte[] content = msg.getContent();
            if (onlineCacheProvider.containsKey(device)) {
                //log.info("下行, 设备[{}], 指令[{}, {}]", device, CommonUtil.cmdFormat(cmd), CommonUtil.bytesToStr(content));

                MsgPipeline pipeline = (MsgPipeline) onlineCacheProvider.get(device);
                pipeline.getContext().writeAndFlush(Unpooled.copiedBuffer(content));

                // 原始指令
                byte[] raw = CommonUtil.buildRaw(new byte[]{(byte) 0xFA, (byte) 0xFA}, new byte[]{(byte) 0xFB}, content, true);

                // 写入kafka
                Map map = new HashMap();
                map.put("id", device);
                map.put("serial", msg.getSerial());
                map.put("cmd", cmd);
                map.put("data", CommonUtil.bytesToStr(raw));
                map.put("timestamp", System.currentTimeMillis());

                String rawData = JacksonUtil.toJson(map);
                kafkaUtilRaw.send(new KafkaMsg(device, rawData));
            }
        }
    }

    public static void send(SendMsg msg) {
        msgPool.add(msg);
    }
}
