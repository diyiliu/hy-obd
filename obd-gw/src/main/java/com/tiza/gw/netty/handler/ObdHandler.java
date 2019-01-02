package com.tiza.gw.netty.handler;

import com.diyiliu.plugin.util.CommonUtil;
import com.diyiliu.plugin.util.SpringUtil;
import com.tiza.gw.protocol.ObdDataProcess;
import com.tiza.gw.support.model.ObdHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * Description: ObdHandler
 * Author: DIYILIU
 * Update: 2018-06-26 09:26
 */

@Slf4j
public class ObdHandler extends ChannelInboundHandlerAdapter {
    private final AttributeKey<String> attributeKey = AttributeKey.valueOf("OBD_KEY");

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        String host = ctx.channel().remoteAddress().toString().trim().replaceFirst("/", "");
        log.info("[{}]建立连接...", host);

        // 断开连接
        ctx.channel().closeFuture().addListener(
                (ChannelFuture future) -> {
                    if (future.isDone()) {
                        log.info("[{}]断开连接...", host);

                        Attribute<String> attribute = ctx.channel().attr(attributeKey);
                        if (attribute.get() != null){
                            String device = attribute.get();

                            log.info("设备[{}]断开连接...", device);
                            ObdDataProcess process  = SpringUtil.getBean("obdDataProcess");
                            process.offline(device);
                        }
                    }
                }
        );
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf buf = (ByteBuf) msg;
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);

        // 解析协议头
        ObdDataProcess process  = SpringUtil.getBean("obdDataProcess");
        ObdHeader header = (ObdHeader) process.dealHeader(bytes);
        if (header == null) {

            return;
        }

        // channel 绑定设备
        String device = header.getDevice();
        Attribute<String> attribute = ctx.channel().attr(attributeKey);
        if (attribute.get() == null){
            attribute.set(device);
        }
        process.keepOnline(device, ctx);

        //log.info("上行, 设备[{}], 指令[{}, {}]", device, CommonUtil.cmdFormat(header.getCmd()), CommonUtil.bytesToStr(header.getContent()));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("服务器异常[{}]!", cause.getMessage());
        cause.printStackTrace();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        String key = ctx.channel().remoteAddress().toString().trim().replaceFirst("/", "");

        // 心跳处理
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (IdleState.READER_IDLE == event.state()) {
                log.info("读超时...[{}]...", key);
                ctx.close();
            } else if (IdleState.WRITER_IDLE == event.state()) {
                log.info("写超时...");
            } else if (IdleState.ALL_IDLE == event.state()) {
                log.info("读/写超时...");
            }
        }
    }
}