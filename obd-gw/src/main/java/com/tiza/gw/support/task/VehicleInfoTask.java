package com.tiza.gw.support.task;

import com.diyiliu.plugin.cache.ICache;
import com.diyiliu.plugin.task.ITask;
import com.tiza.gw.support.jpa.dto.VehicleInfo;
import com.tiza.gw.support.jpa.facade.VehicleInfoJpa;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * Description: VehicleInfoTask
 * Author: DIYILIU
 * Update: 2018-06-28 13:51
 */

@Slf4j
@Component
public class VehicleInfoTask implements ITask {

    @Resource
    private ICache vehicleCacheProvider;

    @Resource
    private VehicleInfoJpa vehicleInfoJpa;

    @Scheduled(fixedDelay = 5 * 60 * 1000, initialDelay = 3 * 1000)
    public void execute() {
        log.info("更新车辆列表缓存 ... ");

        List<VehicleInfo> vehicleInfoList = vehicleInfoJpa.findAll();
        refresh(vehicleInfoList, vehicleCacheProvider);
    }

    private void refresh(List<VehicleInfo> vehicleInfoList, ICache vehicleCache) {
        if (vehicleInfoList == null || vehicleInfoList.size() < 1) {
            log.warn("无车辆信息!");
            return;
        }

        Set oldKeys = vehicleCache.getKeys();
        Set tempKeys = new HashSet(vehicleInfoList.size());

        for (VehicleInfo vehicle : vehicleInfoList) {
            if (vehicle.getTerminalInfo() == null) {
                continue;
            }

            String device = vehicle.getTerminalInfo().getDeviceId();
            vehicleCache.put(device, vehicle);
            tempKeys.add(device);
        }

        Collection subKeys = CollectionUtils.subtract(oldKeys, tempKeys);
        for (Iterator iterator = subKeys.iterator(); iterator.hasNext(); ) {
            String key = (String) iterator.next();
            vehicleCache.remove(key);
        }
    }
}
