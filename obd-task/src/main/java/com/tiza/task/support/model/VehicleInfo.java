package com.tiza.task.support.model;

import com.tiza.task.support.dao.dto.MaintencePolicy;
import lombok.Data;

import java.util.Date;

/**
 * Description: VehicleInfo
 * Author: DIYILIU
 * Update: 2018-07-30 10:14
 */

@Data
public class VehicleInfo {

    private Long vehicleId;

    private Date saleDate;

    private Double mileage;

    private MaintencePolicy maintencePolicy;
}
