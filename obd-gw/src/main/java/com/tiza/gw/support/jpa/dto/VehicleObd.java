package com.tiza.gw.support.jpa.dto;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Description: VehicleObd
 * Author: DIYILIU
 * Update: 2018-08-08 09:16
 */

@Data
@Entity
@Table(name = "VEHICLE_OBD")
public class VehicleObd {

    @Id
    private Long vehicleId;


    @Column(name = "TOTALMILEAGE")
    private Double mileage;

    @Column(name = "TOTALFUELCONSUMPTION")
    private Double fuelConsum;
}
