package com.tiza.gw.netty.handler.codec;

import com.diyiliu.plugin.util.CommonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Description: ObdDecoder
 * Author: DIYILIU
 * Update: 2018-06-26 09:26
 */

@Slf4j
public class ObdDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < 14){

            return;
        }

        // 读标记
        in.markReaderIndex();
        int header1 = in.readUnsignedByte();
        int header2 = in.readUnsignedByte();

        if (header1 != header2 || header2 != 0xFA){
            log.error("包头[{}, {}]校验失败!", CommonUtil.cmdFormat(header1), CommonUtil.cmdFormat(header2));

            ctx.close();
            return;
        }

        // 序列号 + 功能ID + 设备ID
        byte[] headerArray = new byte[8];
        in.readBytes(headerArray);

        int length =  in.readUnsignedShort();
        if (in.readableBytes() < length + 2){

            in.resetReaderIndex();
            return;
        }
        in.resetReaderIndex();

        // 包头
        in.readBytes(new byte[2]);

        byte[] content = new byte[10 + length];
        in.readBytes(content);

        byte check = in.readByte();
        byte check1 = CommonUtil.getCheck(content);

/*
        if (check != check1){
            log.error("校验码错误!");
            ctx.close();
            return;
        }
*/

        int footer = in.readUnsignedByte();
        if (footer != 0xFB){
            log.error("包尾校验失败!");
            ctx.close();
            return;
        }

        out.add(Unpooled.copiedBuffer(content));
    }
}
