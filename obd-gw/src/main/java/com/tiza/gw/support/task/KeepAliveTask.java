package com.tiza.gw.support.task;

import com.diyiliu.plugin.cache.ICache;
import com.diyiliu.plugin.task.ITask;
import com.diyiliu.plugin.util.DateUtil;
import com.tiza.gw.protocol.ObdDataProcess;
import com.tiza.gw.support.model.MsgPipeline;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * Description: KeepAliveTask
 * Author: DIYILIU
 * Update: 2018-06-27 14:43
 */

@Slf4j
@Component
public class KeepAliveTask implements ITask {
    private final static int MSG_IDLE = 10 * 60 * 1000;

    @Resource
    private ICache onlineCacheProvider;

    @Resource
    private ObdDataProcess obdDataProcess;

    @Scheduled(fixedRate = 60 * 1000, initialDelay = 3 * 1000)
    public void execute() {
        log.info("在线检测... ");

        Set keys = onlineCacheProvider.getKeys();
        for (Iterator iterator = keys.iterator(); iterator.hasNext();){
            String key = (String) iterator.next();
            MsgPipeline pipeline = (MsgPipeline) onlineCacheProvider.get(key);
            if (System.currentTimeMillis() - pipeline.getTime() > MSG_IDLE ||
                    !pipeline.getContext().channel().isOpen()){

                log.info("设备[{}]超时离线, 检测时间[{}]", key, DateUtil.dateToString(new Date()));
                obdDataProcess.offline(key);
            }
        }
    }
}
