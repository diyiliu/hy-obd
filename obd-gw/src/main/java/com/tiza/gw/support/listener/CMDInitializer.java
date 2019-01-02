package com.tiza.gw.support.listener;

import com.diyiliu.plugin.model.IDataProcess;
import com.diyiliu.plugin.util.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Description: CMDInitializer
 * Author: DIYILIU
 * Update: 2018-06-27 17:13
 */

@Slf4j
public class CMDInitializer implements ApplicationListener {

    private List<Class> protocols;

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {

        log.info("协议指令初始化..");

        for (Class protocol : protocols) {
            Map parseMap = SpringUtil.getBeansOfType(protocol);
            Set set = parseMap.keySet();
            set.stream().forEach(e -> {

                IDataProcess process = (IDataProcess) parseMap.get(e);
                process.init();
            });
        }
    }

    public void setProtocols(List<Class> protocols) {
        this.protocols = protocols;
    }
}
