package com.tiza.gw.support.jpa.facade;

import com.tiza.gw.support.jpa.dto.VehicleInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Description: VehicleInfoJpa
 * Author: DIYILIU
 * Update: 2018-06-28 11:35
 */

public interface VehicleInfoJpa extends JpaRepository<VehicleInfo, Long> {

    @Transactional
    @Modifying
    @Query("update VehicleInfo v set v.obdInitTime =?2 where v.id=?1")
    void updateObdInitTime(long vehicleId, Date date);
}
