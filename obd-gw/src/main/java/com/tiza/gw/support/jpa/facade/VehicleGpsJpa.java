package com.tiza.gw.support.jpa.facade;

import com.tiza.gw.support.jpa.dto.VehicleGps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * Description: VehicleGpsJpa
 * Author: DIYILIU
 * Update: 2018-06-28 16:47
 */
public interface VehicleGpsJpa extends JpaRepository<VehicleGps, Long> {

    @Transactional
    @Modifying
    @Query("update VehicleGps v set v.status =?2 where v.id=?1")
    void updateStatus(long vehicleId, int status);
}
