package com.tiza.gw.netty.server;

import com.tiza.gw.netty.handler.ObdHandler;
import com.tiza.gw.netty.handler.codec.ObdDecoder;
import com.tiza.gw.netty.handler.codec.ObdEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * Description: ObdServer
 * Author: DIYILIU
 * Update: 2018-06-26 09:26
 */

@Slf4j
public class ObdServer extends Thread {
    private int port;

    public void init() {

        this.start();
    }

    @Override
    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1000)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new IdleStateHandler(10 * 60, 0, 0))
                                    .addLast(new ObdEncoder())
                                    .addLast(new ObdDecoder())
                                    .addLast(new ObdHandler());
                        }
                    });

            ChannelFuture f = b.bind(port).sync();

            log.info("OBD网关启动, 端口[{}]...", port);
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    public void setPort(int port) {
        this.port = port;
    }
}
