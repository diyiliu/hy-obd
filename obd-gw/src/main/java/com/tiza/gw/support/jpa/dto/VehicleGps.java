package com.tiza.gw.support.jpa.dto;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Description: VehicleGps
 * Author: DIYILIU
 * Update: 2018-06-28 16:37
 */

@Data
@Entity
@Table(name = "vehicle_gps")
public class VehicleGps {

    @Id
    private Long vehicleId;

    private Date gpsTime;

    private Date systemTime;

    private Double wgs84lat;

    private Double wgs84lng;

    private Double bd09lat;

    private Double bd09lng;

    private Integer accStatus;

    private Double speed;

    private Integer direction;

    private Integer satellites;

    private Double voltage;

    /** 车辆状态(0停止;1启动;2离线;) **/
    @Column(name = "VEHICLESTATUS")
    private Integer status;
}
