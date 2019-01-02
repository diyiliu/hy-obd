package com.tiza.gw.protocol.cmd;

import com.diyiliu.plugin.model.Header;
import com.tiza.gw.protocol.ObdDataProcess;
import com.tiza.gw.support.model.ObdHeader;
import org.springframework.stereotype.Component;

/**
 * Description: CMD_A4
 * Author: DIYILIU
 * Update: 2018-07-03 09:48
 */

@Component
public class CMD_A4 extends ObdDataProcess {

    public CMD_A4() {
        this.cmd = 0xA4;
    }

    @Override
    public byte[] pack(Header header, Object... argus) {
        ObdHeader obdHeader = (ObdHeader) header;

        return toSendBytes((byte[]) argus[0], cmd, obdHeader);
    }
}
