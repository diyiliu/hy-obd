package com.tiza.das.support.dao.dto;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

/**
 * Description: MaintencePolicy
 * Author: DIYILIU
 * Update: 2018-07-30 09:35
 */

@Data
@MappedSuperclass
public class MaintencePolicy {

    @Transient
    private Long id;


    /** 保养周期里程(km) **/
    private Integer cycleMileage;

    /** 保养周期时间(月) **/
    private Integer cycleMonth;
}
