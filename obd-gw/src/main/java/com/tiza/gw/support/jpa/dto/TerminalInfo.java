package com.tiza.gw.support.jpa.dto;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Description: TerminalInfo
 * Author: DIYILIU
 * Update: 2018-06-28 17:13
 */

@Data
@Entity
@Table(name = "terminal")
public class TerminalInfo {

    @Id
    private Long id;

    private String deviceId;

    private Integer usageStatus;

    private Integer serviceMonth;
}
