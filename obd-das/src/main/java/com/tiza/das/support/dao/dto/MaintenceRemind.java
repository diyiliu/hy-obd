package com.tiza.das.support.dao.dto;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Description: MaintenceRemind
 * Author: DIYILIU
 * Update: 2018-07-30 09:56
 */

@Data
@Entity
@Table(name = "VEHICLE_MAINTENANCE_REMIND")
public class MaintenceRemind {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mr_seq")
    @SequenceGenerator(name="mr_seq", sequenceName = "SQ_VEHICLE_MAINTENANCE_REMIND", allocationSize = 1)
    private Long id;

    private Long vehicleId;

    private Integer mileage;

    private String message;

    /** 保养触发条件(1里程;2时间) **/
    private Integer condition;

    private Date remindDate;
}
