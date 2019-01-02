package com.tiza.gw.protocol.cmd;

import com.diyiliu.plugin.model.Header;
import com.tiza.gw.protocol.ObdDataProcess;
import com.tiza.gw.support.model.ObdHeader;
import org.springframework.stereotype.Component;

/**
 * Description: CMD_01
 * Author: DIYILIU
 * Update: 2018-06-26 10:56
 */

@Component
public class CMD_01 extends ObdDataProcess {

    public CMD_01() {
        this.cmd = 0x01;
    }

    @Override
    public void parse(byte[] content, Header header) {
        ObdHeader obdHeader = (ObdHeader) header;



    }
}
