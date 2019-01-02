package com.tiza.gw.protocol;

import com.diyiliu.plugin.cache.ICache;
import com.diyiliu.plugin.model.Header;
import com.diyiliu.plugin.model.IDataProcess;
import com.diyiliu.plugin.model.SendMsg;
import com.diyiliu.plugin.util.CommonUtil;
import com.diyiliu.plugin.util.JacksonUtil;
import com.tiza.gw.support.client.KafkaUtil;
import com.tiza.gw.support.client.RedisClient;
import com.tiza.gw.support.jpa.dto.VehicleInfo;
import com.tiza.gw.support.jpa.facade.VehicleInfoJpa;
import com.tiza.gw.support.model.KafkaMsg;
import com.tiza.gw.support.model.MsgPipeline;
import com.tiza.gw.support.model.ObdHeader;
import com.tiza.gw.support.task.MsgSenderTask;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * Description: ObdDataProcess
 * Author: DIYILIU
 * Update: 2018-06-26 10:47
 */

@Slf4j
@Service
public class ObdDataProcess implements IDataProcess {

    protected int cmd = 0xFF;

    @Value("${obd.host}")
    private String host;

    @Resource
    private ICache vehicleCacheProvider;

    @Resource
    private ICache onlineCacheProvider;

    @Resource
    private VehicleInfoJpa vehicleInfoJpa;

    @Resource
    private KafkaUtil kafkaUtil;

    @Resource
    private RedisClient redisClient;

    // 响应指令
    private List respCmds = new ArrayList() {
        {
            this.add(0x00);
            this.add(0x01);
            this.add(0x02);
            this.add(0x03);
            this.add(0x05);
            this.add(0x06);
            this.add(0x07);
            this.add(0x08);
            this.add(0x09);
            this.add(0x0A);
            this.add(0x0B);
            this.add(0x0C);
            this.add(0x0D);
            this.add(0x0E);
            this.add(0x0F);
            this.add(0x95);
            this.add(0x9E);
            this.add(0xA0);
            this.add(0xA1);
            this.add(0xA8);
            this.add(0xA9);
            this.add(0xAA);
            this.add(0xAB);
            this.add(0xAC);
            this.add(0xE8);
            this.add(0xE9);
            this.add(0xEA);
            this.add(0xEB);
            this.add(0xEC);
        }
    };

    @Override
    public Header dealHeader(byte[] bytes) {
        ByteBuf buf = Unpooled.copiedBuffer(bytes);

        int serial = buf.readUnsignedByte();
        int cmd = buf.readUnsignedByte();

        byte[] deviceArray = new byte[6];
        buf.readBytes(deviceArray);
        String device = CommonUtil.parseSIM(deviceArray);

        // 写入kafka
        toKafka(device, serial, cmd, bytes);

        if (!vehicleCacheProvider.containsKey(device)) {
            log.warn("设备[{}]未注册!", device);

            return null;
        }

        int length = buf.readUnsignedShort();
        byte[] content = new byte[length];
        buf.readBytes(content);

        ObdHeader header = new ObdHeader();
        header.setSerial(serial);
        header.setCmd(cmd);
        header.setDevice(device);
        header.setLength(length);
        header.setContent(content);

        // 响应数据
        toResp(header);

        return header;
    }

    @Override
    public void parse(byte[] content, Header header) {

    }

    @Override
    public byte[] pack(Header header, Object... argus) {
        return new byte[0];
    }

    @Override
    public void init() {
        // cmdCacheProvider.put(this.cmd, this);
    }

    /**
     * 保持在线
     *
     * @param device
     * @param ctx
     */
    public void keepOnline(String device, ChannelHandlerContext ctx) {
        onlineCacheProvider.put(device, new MsgPipeline(ctx, System.currentTimeMillis()));

        redisClient.set("obd:t:" + device, "gateway:" + host);
    }

    private void toKafka(String deviceId, int serial, int cmd, byte[] content) {
        long time = System.currentTimeMillis();
        // 原始指令
        byte[] raw = CommonUtil.buildRaw(new byte[]{(byte) 0xFA, (byte) 0xFA}, new byte[]{(byte) 0xFB}, content, true);

        Map map = new HashMap();
        map.put("id", deviceId);
        map.put("serial", serial);
        map.put("cmd", cmd);
        map.put("data", CommonUtil.bytesToStr(raw));
        map.put("timestamp", time);

        // 写入kafka
        String rawData = JacksonUtil.toJson(map);
        //log.info("设备[{}], 指令[{}]数据写入kafka ...", deviceId, CommonUtil.cmdFormat(cmd));
        kafkaUtil.send(new KafkaMsg(deviceId, rawData));
    }

    private void toResp(ObdHeader header) {
        int cmd = header.getCmd();
        if (respCmds.contains(cmd)) {
            send(0x80, new byte[]{(byte) cmd}, header);

            // 设置里程和油耗
            if (0x0A == cmd) {
                VehicleInfo vehicleInfo = (VehicleInfo) vehicleCacheProvider.get(header.getDevice());
                // 获取实时数据
                vehicleInfo = vehicleInfoJpa.findById(vehicleInfo.getId()).get();

                // 设置初始里程
                if (vehicleInfo.getObdInitTime() == null) {

                    ByteBuf buf = Unpooled.buffer(8);
                    // 初始总里程
                    buf.writeInt(vehicleInfo.getInitialMileage() == null ? 0 : vehicleInfo.getInitialMileage() * 1000);
                    // 初始总油耗
                    Double fuelConsum = vehicleInfo.getVehicleObd().getFuelConsum();
                    buf.writeInt(fuelConsum == null ? 0 : Double.valueOf(fuelConsum * 1000).intValue());
                    send(0x5C, buf.array(), header);

                    vehicleInfoJpa.updateObdInitTime(vehicleInfo.getId(), new Date());
                }

                // 计算服务时间
                if (vehicleInfo.getServiceBegin() == null) {
                    Integer month = vehicleInfo.getTerminalInfo().getServiceMonth();
                    Calendar calendar = Calendar.getInstance();
                    Date begin = calendar.getTime();
                    calendar.add(Calendar.MONTH, month == null ? 6 : month);
                    Date end = calendar.getTime();

                    vehicleInfo.setServiceBegin(begin);
                    vehicleInfo.setServiceEnd(end);
                    vehicleInfoJpa.save(vehicleInfo);
                }
            }
        } else {

            if (0x04 == cmd || 0x97 == cmd) {
                byte[] content = header.getContent();

                byte[] bytes = new byte[3];
                System.arraycopy(content, 26, bytes, 0, 3);
                send(0xA4, bytes, header);

                return;
            }

            if (0x7F == cmd) {
                byte[] content = header.getContent();
                byte result = content[0];
                if (result == 0) {

                    log.info("参数设置成功!");
                } else {
                    log.info("参数设置失败!");
                }

                return;
            }

            // 获取参数设置
            if (0x93 == cmd) {
                VehicleInfo vehicleInfo = (VehicleInfo) vehicleCacheProvider.get(header.getDevice());
                ByteBuf buf = Unpooled.buffer(16);
                // 车系
                buf.writeByte(vehicleInfo.getBrandId() == null ? 0 : vehicleInfo.getBrandId());
                // 车型
                buf.writeByte(vehicleInfo.getProductId() == null ? 0 : vehicleInfo.getProductId());
                // 默认 2015
                buf.writeByte(0x0F);
                // 变速箱类型
                buf.writeByte(0);
                // 特殊处理
                buf.writeByte(0);
                // 汽车排量
                buf.writeByte(vehicleInfo.getEngineCapacity() == null ? 0 : (int) (vehicleInfo.getEngineCapacity() * 10));
                // 熄火休眠时间
                buf.writeShort(0);
                // 初始总里程
                buf.writeInt(vehicleInfo.getInitialMileage() == null ? 0 : vehicleInfo.getInitialMileage() * 1000);
                // 初始总油耗
                buf.writeInt(0);

                send(0x83, buf.array(), header);
                vehicleInfoJpa.updateObdInitTime(vehicleInfo.getId(), new Date());

                return;
            }

            // 获取时间
            if (0x94 == cmd) {
                Date utc = reviseTime(Calendar.getInstance(), -1);
                byte[] content = initDate(utc);
                send(0x84, content, header);

                return;
            }
        }
    }


    protected byte[] toSendBytes(byte[] content, int cmd, ObdHeader header) {
        int length = content.length;

        ByteBuf buf = Unpooled.buffer(10 + length);
        buf.writeByte(header.getSerial());
        buf.writeByte(cmd);
        buf.writeBytes(CommonUtil.packSIM(header.getDevice(), 6));
        buf.writeShort(length);
        buf.writeBytes(content);

        return buf.array();
    }

    private void send(int cmd, byte[] content, ObdHeader header) {
        SendMsg msg = new SendMsg();
        msg.setCmd(cmd);
        msg.setSerial(header.getSerial());
        msg.setDevice(header.getDevice());
        msg.setContent(toSendBytes(content, cmd, header));

        MsgSenderTask.send(msg);
    }

    private byte[] initDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        byte[] bytes = new byte[6];
        bytes[0] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
        bytes[1] = (byte) calendar.get(Calendar.MINUTE);
        bytes[2] = (byte) calendar.get(Calendar.SECOND);

        bytes[3] = (byte) (calendar.get(Calendar.YEAR) - 2000);
        bytes[4] = (byte) (calendar.get(Calendar.MONTH) + 1);
        bytes[5] = (byte) calendar.get(Calendar.DAY_OF_MONTH);

        return bytes;
    }

    /**
     * 修正时差
     *
     * @param calendar
     * @param offset   (1: 正偏移, -1: 负偏移)
     * @return
     */
    private Date reviseTime(Calendar calendar, int offset) {
        // 1、取得时间偏移量：
        int zoneOffset = calendar.get(Calendar.ZONE_OFFSET);
        // 2、取得夏令时差：
        int dstOffset = calendar.get(Calendar.DST_OFFSET);
        // 3、从本地时间里扣除这些差量，即可以取得UTC时间：
        calendar.add(Calendar.MILLISECOND, offset * (zoneOffset + dstOffset));

        return calendar.getTime();
    }


    /**
     * 设备离线通知
     *
     * @param device
     */
    public void offline(String device) {
        onlineCacheProvider.remove(device);
    }

/*
    private byte[] initBytes(VehicleInfo vehicleInfo) {
        ByteBuf buf = Unpooled.buffer(16);
        // 车系
        buf.writeByte(vehicleInfo.getBrandId() == null ? 0 : vehicleInfo.getBrandId());
        // 车型
        buf.writeByte(vehicleInfo.getProductId() == null ? 0 : vehicleInfo.getProductId());
        // 默认 2015
        buf.writeByte(0x0F);
        // 变速箱类型
        buf.writeByte(0);
        // 特殊处理
        buf.writeByte(0);
        // 汽车排量
        buf.writeByte(vehicleInfo.getEngineCapacity() == null ? 0 : (int) (vehicleInfo.getEngineCapacity() * 10));
        // 熄火休眠时间
        buf.writeShort(0);
        // 初始总里程
        buf.writeInt(vehicleInfo.getInitialMileage() == null ? 0 : vehicleInfo.getInitialMileage() * 1000);
        // 初始总油耗
        buf.writeInt(0);

        return buf.array();
    }
*/

/*
    private void setup(ObdHeader header) {
        ByteBuf buf = Unpooled.buffer();
        buf.writeBytes("1".getBytes());
        buf.writeByte(0x2C);
        buf.writeBytes("0".getBytes());
        buf.writeByte(0x2C);
        buf.writeBytes("218.3.247.227".getBytes());
        buf.writeByte(0x2C);
        buf.writeBytes("5005".getBytes());
        buf.writeByte(0x2C);
        buf.writeBytes("0.0.0.0".getBytes());
        buf.writeByte(0x2C);
        buf.writeBytes("cmnet".getBytes());
        buf.writeByte(0x2C);
        buf.writeByte(0x2C);
        buf.writeByte(0x2C);

        byte[] content = new byte[buf.writerIndex()];
        buf.readBytes(content);

        // 设置通讯参数
        send(0x57, content, header);
    }
*/

/*
    protected void updateGps(VehicleGps vehicleGps) {
        String device = vehicleGps.getDeviceId();
        if (vehicleCacheProvider.containsKey(device)) {
            VehicleInfo vehicleInfo = (VehicleInfo) vehicleCacheProvider.get(device);

            vehicleGps.setVehicleId(vehicleInfo.getId());
            vehicleGpsJpa.save(vehicleGps);

        }
    }
 */
}
