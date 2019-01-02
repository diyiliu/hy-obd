package com.tiza.gw.support.jpa.dto;

import lombok.Data;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.util.Date;

/**
 * Description: VehicleInfo
 * Author: DIYILIU
 * Update: 2018-06-28 11:34
 */

@Data
@Entity
@Table(name = "vehicle")
public class VehicleInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vehicle_seq")
    @SequenceGenerator(name = "vehicle_seq", sequenceName = "sq_vehicle", allocationSize = 1)
    private Long id;

    @OneToOne
    @JoinColumn(name = "terminalId")
    @NotFound(action = NotFoundAction.IGNORE)
    private TerminalInfo terminalInfo;

    private Date obdInitTime;

    private Integer brandId;

    @Column(name = "modelId")
    private Integer productId;

    private Integer initialMileage;

    private Double engineCapacity;

    private Date serviceBegin;

    private Date serviceEnd;

    @OneToOne
    @PrimaryKeyJoinColumn(name = "id", referencedColumnName = "vehicleId")
    private VehicleGps vehicleGps;

    @OneToOne
    @PrimaryKeyJoinColumn(name = "id", referencedColumnName = "vehicleId")
    private VehicleObd vehicleObd;
}
