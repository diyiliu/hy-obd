package com.tiza.gw.support.model;

import com.diyiliu.plugin.model.Header;
import lombok.Data;

/**
 * Description: ObdHeader
 * Author: DIYILIU
 * Update: 2018-06-26 11:21
 */

public class ObdHeader extends Header {
    private Integer cmd;

    private String device;

    private Integer serial;

    private Integer length;

    private byte[] content = new byte[0];

    public Integer getCmd() {
        return cmd;
    }

    public void setCmd(Integer cmd) {
        this.cmd = cmd;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public Integer getSerial() {
        return serial;
    }

    public void setSerial(Integer serial) {
        this.serial = serial;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
