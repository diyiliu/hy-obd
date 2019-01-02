package com.tiza.task.support.dao.jpa;

import com.tiza.task.support.dao.dto.MaintenceProgress;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Description: MaintenceProgressJpa
 * Author: DIYILIU
 * Update: 2018-08-13 13:48
 */
public interface MaintenceProgressJpa extends JpaRepository<MaintenceProgress, Long> {


    MaintenceProgress findByVehicleId(long vehicleId);
}
