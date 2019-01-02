package com.tiza.task.support.dao.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Description: DefaultPolicy
 * Author: DIYILIU
 * Update: 2018-07-30 09:44
 */

@Entity
@Table(name = "MAINTENANCE_POLICY")
public class DefaultPolicy extends MaintencePolicy {

    @Id
    private Long id;

    private String name;

    @Column(name = "FIRSTMILEAGE")
    private Integer mileage1;

    @Column(name = "FIRSTMONTH")
    private Integer month1;

    @Column(name = "SECONDMILEAGE")
    private Integer mileage2;

    @Column(name = "SECONDMONTH")
    private Integer month2;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
