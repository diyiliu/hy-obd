package com.tiza.gw.protocol.cmd;

import com.diyiliu.plugin.model.Header;
import com.tiza.gw.protocol.ObdDataProcess;
import com.tiza.gw.support.model.ObdHeader;
import org.springframework.stereotype.Component;

/**
 * Description: CMD_80
 * Author: DIYILIU
 * Update: 2018-06-26 15:09
 */

@Component
public class CMD_80 extends ObdDataProcess {

    public CMD_80() {
        this.cmd = 0x80;
    }

    @Override
    public byte[] pack(Header header, Object... argus) {
        ObdHeader obdHeader = (ObdHeader) header;

        return toSendBytes(new byte[]{obdHeader.getCmd().byteValue()}, cmd, obdHeader);
    }
}
