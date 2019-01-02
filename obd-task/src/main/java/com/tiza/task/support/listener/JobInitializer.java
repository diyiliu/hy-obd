package com.tiza.task.support.listener;

import com.diyiliu.plugin.task.ITask;
import com.diyiliu.plugin.util.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * Description: JobInitializer
 * Author: DIYILIU
 * Update: 2018-06-27 17:13
 */

@Slf4j
public class JobInitializer implements ApplicationListener {

    private String jobList;

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        log.info("执行任务 ... ");

        String[] array = jobList.split(",");
        for (String job: array){
            ITask task = SpringUtil.getBean(job);
            task.execute();
        }
    }

    public void setJobList(String jobList) {
        this.jobList = jobList;
    }
}
