package com.tiza.das.support.task;

import com.diyiliu.plugin.task.ITask;
import com.diyiliu.plugin.util.CommonUtil;
import com.diyiliu.plugin.util.JacksonUtil;
import com.tiza.das.support.dao.dto.*;
import com.tiza.das.support.dao.jpa.*;
import com.tiza.das.support.model.KafkaMsg;
import com.tiza.das.support.model.VehicleInfo;
import com.tiza.das.support.util.KafkaUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Description: MaintainTask
 * Author: DIYILIU
 * Update: 2018-07-30 09:52
 */

@Slf4j
@Component
public class MaintainTask implements ITask {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private CustomPolicyJpa customPolicyJpa;

    @Resource
    private DefaultPolicyJpa defaultPolicyJpa;

    @Resource
    private MaintenceRemindJpa maintenceRemindJpa;

    @Resource
    private MaintenceProgressJpa maintenceProgressJpa;

    @Resource
    private MaintenceRecordJpa maintenceRecordJpa;

    @Resource
    private KafkaUtil kafkaUtil;


    @Scheduled(cron = "30 20 1 * * ?")
    public void execute() {
        log.info("车辆保养分析... ");

        List<DefaultPolicy> defaultPolicyList = defaultPolicyJpa.findAll();
        Map<Long, DefaultPolicy> defaultMap = defaultPolicyList.stream().collect(Collectors.toMap(DefaultPolicy::getId, d -> d));

        List<CustomPolicy> customPolicyList = customPolicyJpa.findAll();
        Map<Long, CustomPolicy> customMap = customPolicyList.stream().collect(Collectors.toMap(CustomPolicy::getVehicleId, c -> c));

        List<VehicleInfo> vehicleInfoList = fetchVehicleInfo(defaultMap, customMap);
        for (VehicleInfo vehicleInfo : vehicleInfoList) {
            MaintencePolicy policy = vehicleInfo.getMaintencePolicy();
            if (policy == null || vehicleInfo.getMileage() == null) {
                log.warn("车辆[{}]未绑定保养策略或无法获取当前里程!", vehicleInfo.getVehicleId());
                continue;
            }

            if (policy instanceof DefaultPolicy) {
                continue;
            }

            MaintenceProgress mtProgress = maintenceProgressJpa.findByVehicleId(vehicleInfo.getVehicleId());
            // 首次检测
            if (mtProgress == null) {
                initCheck((CustomPolicy) policy, vehicleInfo);
            } else {
                int mileage = vehicleInfo.getMileage().intValue();

                // 修正异常进度条
                if (mileage > mtProgress.getEndMileage() + policy.getCycleMileage()){
                    maintenceProgressJpa.delete(mtProgress);
                    initCheck((CustomPolicy) policy, vehicleInfo);
                }

                Date now = new Date();
                // 根据保养记录,修正保养提醒
                List<MaintenceRecord> records = maintenceRecordJpa.findByVehicleId(vehicleInfo.getVehicleId(), Sort.by(Sort.Direction.DESC, "mileage"));
                if (CollectionUtils.isNotEmpty(records)) {
                    int recordMileage = records.get(0).getMileage();
                    if (mileage > recordMileage && recordMileage > mtProgress.getStartMileage()) {

                        mtProgress.setStartMileage(recordMileage);
                        mtProgress.setEndMileage(recordMileage + policy.getCycleMileage());
                        mtProgress.setStartDate(now);
                    }
                }

                if (mileage > mtProgress.getEndMileage()) {

                    int startMile, endMile;

                    startMile = mtProgress.getEndMileage();
                    endMile = mtProgress.getEndMileage() + policy.getCycleMileage();

                    // 保养进度
                    mtProgress.setStartMileage(startMile);
                    mtProgress.setEndMileage(endMile);
                    mtProgress.setStartDate(now);

                    // 保养提醒
                    MaintenceRemind remind = new MaintenceRemind();
                    remind.setVehicleId(vehicleInfo.getVehicleId());
                    remind.setCondition(1);
                    remind.setMileage(mileage);
                    remind.setRemindDate(now);
                    remind.setMessage("里程已达到" + startMile + "公里, 请及时保养!");
                    maintenceRemindJpa.save(remind);


                    Map pushMap = new HashMap();
                    pushMap.put("vehicleId", vehicleInfo.getVehicleId());
                    pushMap.put("time", new Date());
                    pushMap.put("event", 5);
                    pushMap.put("message", remind.getMessage());

                    kafkaUtil.send(new KafkaMsg(vehicleInfo.getVehicleId() + "", JacksonUtil.toJson(pushMap)));
                }

                mtProgress.setMileage(mileage);
                mtProgress.setModifyTime(now);
                maintenceProgressJpa.save(mtProgress);
            }
        }
    }

    private void initCheck(CustomPolicy policy, VehicleInfo vehicleInfo) {
        int initMileage = policy.getInitMileage() == null ? 0 : policy.getInitMileage();
        List<MaintenceRecord> records = maintenceRecordJpa.findByVehicleId(vehicleInfo.getVehicleId(), Sort.by(Sort.Direction.DESC, "mileage"));
        if (CollectionUtils.isNotEmpty(records)) {

            initMileage = records.get(0).getMileage();
        }

        int mileGap = policy.getCycleMileage();
        if (initMileage < 1 && mileGap < 1000){
            log.warn("车辆[{}]保养数据异常!", vehicleInfo.getVehicleId());

            return;
        }

        int nowMileage = vehicleInfo.getMileage().intValue();
        int i = 0;
        while (true) {
            int mileage = initMileage + i * mileGap;
            if (mileage > nowMileage) {

                break;
            }
            i++;
        }

        Date sysTime = new Date();
        MaintenceProgress mtProgress = new MaintenceProgress();
        mtProgress.setVehicleId(vehicleInfo.getVehicleId());
        mtProgress.setStartDate(sysTime);
        mtProgress.setModifyTime(sysTime);
        mtProgress.setMileage(nowMileage);
        if (i > 1) {

            mtProgress.setStartMileage(initMileage + (i - 2) * mileGap);
            mtProgress.setEndMileage(initMileage + (i - 1) * mileGap);
        } else {
            mtProgress.setStartMileage(initMileage);
            mtProgress.setEndMileage(initMileage + mileGap);
        }

        maintenceProgressJpa.save(mtProgress);
    }

/*
    private void firstCheck(MaintencePolicy policy, VehicleInfo vehicleInfo) {
        int mileGap = policy.getCycleMileage();
        int monthGap = policy.getCycleMonth();

        int nowMileage = vehicleInfo.getMileage().intValue();
        List list = new ArrayList();
        int i = 0;
        int index = -1;
        boolean flag = true;
        while (flag) {
            if (i == 0) {
                list.add(new Object[]{policy.getMileage1(), policy.getMonth1()});
            } else if (i == 1) {
                list.add(new Object[]{policy.getMileage2(), policy.getMonth2()});
            } else {
                list.add(new Object[]{policy.getMileage2() + (i - 1) * mileGap, policy.getMonth2() + (i - 1) * monthGap});
            }
            i++;

            for (int j = list.size() - 1; j > -1; j--) {
                Object[] objects = (Object[]) list.get(j);
                int mileage = (int) objects[0];

                if (mileage > nowMileage) {
                    index = j;
                    flag = false;
                    break;
                }
            }
        }

        Date sysTime = new Date();
        MaintenceProgress mtProgress = new MaintenceProgress();
        mtProgress.setVehicleId(vehicleInfo.getVehicleId());
        mtProgress.setStartDate(sysTime);
        mtProgress.setModifyTime(sysTime);
        mtProgress.setMileage(nowMileage);
        if (index > 0) {
            Object[] next = (Object[]) list.get(index);
            int nextMileage = (int) next[0];

            Object[] now = (Object[]) list.get(index - 1);
            int mileage = (int) now[0];

            mtProgress.setStartMileage(mileage);
            mtProgress.setEndMileage(nextMileage);
        } else {
            mtProgress.setStartMileage(0);
            mtProgress.setEndMileage(policy.getMileage1());
        }

        if (index > -1) {
            MaintenceRemind remind = new MaintenceRemind();
            remind.setVehicleId(vehicleInfo.getVehicleId());
            remind.setCondition(1);
            remind.setMileage(nowMileage);
            remind.setRemindDate(sysTime);
            remind.setMessage("里程已达到" + mtProgress.getStartMileage() + "公里, 请及时保养!");

            maintenceRemindJpa.save(remind);
        }

        maintenceProgressJpa.save(mtProgress);
    }
*/

    private List<VehicleInfo> fetchVehicleInfo(Map<Long, DefaultPolicy> defaultMap, Map<Long, CustomPolicy> customMap) {
        String sql = "SELECT t.id, t.saletime, o.totalmileage, s.maintenancepolicyid" +
                "  FROM vehicle t" +
                " INNER JOIN vehicle_obd o" +
                "    ON o.vehicleid = t.id" +
                "  LEFT JOIN vehicle_style s" +
                "    ON s.id = t.styleid";

        return jdbcTemplate.query(sql, (ResultSet rs, int i) -> {
            long id = rs.getLong("id");
            Date time = rs.getDate("saletime");
            Double mileage = rs.getDouble("totalmileage");
            Long mpId = rs.getLong("maintenancepolicyid");

            VehicleInfo vehicleInfo = new VehicleInfo();
            vehicleInfo.setVehicleId(id);
            vehicleInfo.setSaleDate(time);
            vehicleInfo.setMileage(mileage);

            MaintencePolicy policy = null;
            if (mpId != null && defaultMap.containsKey(mpId)) {
                policy = defaultMap.get(mpId);
            }
            if (customMap.containsKey(id)) {
                policy = customMap.get(id);
            }
            vehicleInfo.setMaintencePolicy(policy);

            return vehicleInfo;
        });
    }


    private double calcMonth(Date date1, Date date2) {
        Calendar past = Calendar.getInstance();
        past.setTime(date1);
        Calendar now = Calendar.getInstance();
        now.setTime(date2);
        int year = now.get(Calendar.YEAR) - past.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) - past.get(Calendar.MONTH);
        double day = (now.get(Calendar.DAY_OF_MONTH) - past.get(Calendar.DAY_OF_MONTH)) / 30;

        return CommonUtil.keepDecimal(year * 12 + month + day, 1);
        /*
        // 秒
        double interval = (date2.getTime() - date1.getTime()) * 0.001;
        // 月
        return CommonUtil.keepDecimal(interval / (3600 * 24 * 30), 1);
        */
    }
}
