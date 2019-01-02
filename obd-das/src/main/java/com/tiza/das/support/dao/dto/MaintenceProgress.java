package com.tiza.das.support.dao.dto;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Description: MaintenceProgress
 * Author: DIYILIU
 * Update: 2018-08-13 09:46
 */

@Data
@Entity
@Table(name = "MAINTENANCE_PROGRESS")
public class MaintenceProgress {

    @Id
    private Long vehicleId;

    private Integer mileage;

    private Integer startMileage;

    private Integer endMileage;

    private Date startDate;

    private Date modifyTime;
}
