package com.tiza.gw.protocol.cmd;

import com.diyiliu.plugin.model.Header;
import com.diyiliu.plugin.util.CommonUtil;
import com.diyiliu.plugin.util.GpsCorrectUtil;
import com.tiza.gw.protocol.ObdDataProcess;
import com.tiza.gw.support.jpa.dto.VehicleGps;
import com.tiza.gw.support.model.ObdHeader;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Description: CMD_00
 * Author: DIYILIU
 * Update: 2018-06-26 10:56
 */

@Component
public class CMD_00 extends ObdDataProcess {

    public CMD_00() {
        this.cmd = 0x00;
    }

    @Override
    public void parse(byte[] content, Header header) {
/*
        if (content.length < 24) {

            log.error("GPS数据包长度不足!");
            return;
        }
        ObdHeader obdHeader = (ObdHeader) header;
        ByteBuf buf = Unpooled.copiedBuffer(content);

        byte[] dateBytes = new byte[6];
        buf.readBytes(dateBytes);
        Date gpsTime = formatDate(dateBytes);

        long lat = buf.readUnsignedInt();
        long lng = buf.readUnsignedInt();

        double latD = formatLatLng(lat) * 0.000001;
        double lngD = formatLatLng(lng) * 0.000001;

        double[] bd09 = transformLatLng(latD, lngD);
        double bd09lat = bd09[0];
        double bd09lng = bd09[1];

        byte[] altBytes = new byte[3];
        buf.readBytes(altBytes);

        long alt = CommonUtil.bytesToLong(altBytes);

        double speed = buf.readUnsignedShort() * 0.1;
        double direction = buf.readUnsignedShort() * 0.1;

        int satellites = buf.readUnsignedByte();

        int output = buf.readUnsignedByte();
        int accStatus = buf.readUnsignedByte();

        double battery = buf.readUnsignedShort() * 0.1;

        VehicleGps vehicleGps = new VehicleGps();
        vehicleGps.setDeviceId(obdHeader.getDevice());
        vehicleGps.setWgs84lat(latD);
        vehicleGps.setWgs84lng(lngD);
        vehicleGps.setBd09lat(bd09lat);
        vehicleGps.setBd09lng(bd09lng);
        vehicleGps.setSpeed(speed);
        // 取整
        vehicleGps.setDirection(Integer.valueOf(String.format("%.0f", direction)));
        vehicleGps.setSatellites(satellites);
        vehicleGps.setAccStatus(accStatus);
        vehicleGps.setVoltage(battery);
        vehicleGps.setGpsTime(gpsTime);
        vehicleGps.setSystemTime(new Date());

        // 更新GPS
        updateGps(vehicleGps);

        // 响应
        send(0x80, obdHeader);
*/
    }

    private long formatLatLng(long l) {
        long i = l >> 31;

        return (l & 0x7FFFFFFF) * (i == 0 ? 1 : -1);
    }

    private double[] transformLatLng(double lat, double lng){

        double[] latLng_gcj = GpsCorrectUtil.gps84_To_Gcj02(lat, lng);

        return GpsCorrectUtil.gcj02_To_Bd09(latLng_gcj[0], latLng_gcj[1]);
    }
}
