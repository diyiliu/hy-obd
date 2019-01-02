package com.tiza.das.support.dao.jpa;

import com.tiza.das.support.dao.dto.MaintenceRemind;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Description: MaintenceRemindJpa
 * Author: DIYILIU
 * Update: 2018-07-30 11:24
 */
public interface MaintenceRemindJpa extends JpaRepository<MaintenceRemind, Long> {

    List<MaintenceRemind> findByVehicleId(long vehicleId, Sort sort);
}
