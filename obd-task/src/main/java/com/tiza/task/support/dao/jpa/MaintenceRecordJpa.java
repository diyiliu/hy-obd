package com.tiza.task.support.dao.jpa;

import com.tiza.task.support.dao.dto.MaintenceRecord;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Description: MaintenceRecordJpa
 * Author: DIYILIU
 * Update: 2018-07-30 11:24
 */
public interface MaintenceRecordJpa extends JpaRepository<MaintenceRecord, Long> {

    List<MaintenceRecord> findByVehicleId(long vehicleId, Sort sort);
}
