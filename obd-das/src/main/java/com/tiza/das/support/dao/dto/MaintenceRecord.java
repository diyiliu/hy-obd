package com.tiza.das.support.dao.dto;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Description: MaintenceRecord
 * Author: DIYILIU
 * Update: 2018-09-11 10:20
 */

@Data
@Entity
@Table(name = "MAINTENANCE_RECORD")
public class MaintenceRecord {

    @Id
    private Long id;

    private Long vehicleId;

    @Column(name = "MAINTENANCEDATE")
    private Date maintenceDate;

    private Integer mileage;
}
