package com.tiza.gw.support.model;

import io.netty.channel.ChannelHandlerContext;

/**
 * Description: MsgPipeline
 * Author: DIYILIU
 * Update: 2018-06-27 14:03
 */

public class MsgPipeline {
    private ChannelHandlerContext context;

    private Long time;

    public MsgPipeline() {

    }

    public MsgPipeline(ChannelHandlerContext context, Long time) {
        this.context = context;
        this.time = time;
    }

    public ChannelHandlerContext getContext() {
        return context;
    }

    public void setContext(ChannelHandlerContext context) {
        this.context = context;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
