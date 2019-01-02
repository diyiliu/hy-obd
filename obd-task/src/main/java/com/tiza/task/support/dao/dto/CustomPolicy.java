package com.tiza.task.support.dao.dto;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Description: CustomPolicy
 * Author: DIYILIU
 * Update: 2018-07-30 09:44
 */

@Entity
@Table(name = "CUSTOM_MAINTENANCE")
public class CustomPolicy extends MaintencePolicy {

    @Id
    private Long id;

    private Long vehicleId;

    private Integer initMileage;

    private Date initDate;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Integer getInitMileage() {
        return initMileage;
    }

    public void setInitMileage(Integer initMileage) {
        this.initMileage = initMileage;
    }

    public Date getInitDate() {
        return initDate;
    }

    public void setInitDate(Date initDate) {
        this.initDate = initDate;
    }
}
