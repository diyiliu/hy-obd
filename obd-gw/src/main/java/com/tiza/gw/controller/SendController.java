package com.tiza.gw.controller;

import com.diyiliu.plugin.cache.ICache;
import com.diyiliu.plugin.model.SendMsg;
import com.diyiliu.plugin.util.CommonUtil;
import com.tiza.gw.support.task.MsgSenderTask;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * Description: SendController
 * Author: DIYILIU
 * Update: 2018-04-16 09:10
 */

@Slf4j
@RestController
@Api(description = "设备指令下发接口")
public class SendController {

    @Resource
    private ICache onlineCacheProvider;

    /**
     * 参数设置
     *
     * @param cmd
     * @param value
     * @param device
     * @param response
     * @return
     */
    @PostMapping("/setup")
    @ApiOperation(value = "参数设置", notes = "设置设备参数")
    public String setup(@RequestParam(name = "serial", required = false) Integer serial,
                        @RequestParam("cmd") Integer cmd, @RequestParam("content") String value,
                        @RequestParam("device") String device, HttpServletResponse response) {

        if (!onlineCacheProvider.containsKey(device)) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return "设备离线。";
        }

        SendMsg msg = new SendMsg();
        msg.setCmd(cmd);
        msg.setDevice(device);
        msg.setSerial(serial);
        msg.setContent(CommonUtil.hexStringToBytes(value));
        MsgSenderTask.send(msg);

        return "设置成功。";
    }
}
