package com.tiza.gw.netty.handler.codec;

import com.diyiliu.plugin.util.CommonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * Description: ObdEncoder
 * Author: DIYILIU
 * Update: 2018-06-26 09:27
 */

@Slf4j
public class ObdEncoder extends MessageToByteEncoder {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) {
        ByteBuf buf = (ByteBuf) msg;

        int length = buf.readableBytes();

        byte[] content = new byte[length];
        buf.readBytes(content);

        byte check = CommonUtil.getCheck(content);

        ByteBuf byteBuf = Unpooled.buffer(2 + length + 2);
        byteBuf.writeByte(0xFA);
        byteBuf.writeByte(0xFA);
        byteBuf.writeBytes(content);
        byteBuf.writeByte(check);
        byteBuf.writeByte(0xFB);

        out.writeBytes(byteBuf);
    }
}
