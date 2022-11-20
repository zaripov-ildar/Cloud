package ru.starstreet.cloud.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;
import ru.starstreet.cloud.core.AbstractMessage;
import ru.starstreet.cloud.core.OnMessageReceived;

@Slf4j
public class NettyClient {
    private SocketChannel channel;

    public NettyClient(OnMessageReceived callback) {
        Thread t = new Thread(() -> {
            EventLoopGroup group = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap();
                ChannelFuture future = bootstrap.channel(NioSocketChannel.class)
                        .group(group)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) {
                                channel = ch;
                                ch.pipeline().addLast(
                                        new ObjectEncoder(),
                                        new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                        new ClientMessageHandler(callback)
                                );
                            }
                        }).connect("localhost", 8189).sync();
                future.channel().closeFuture().sync();
            } catch (Exception e) {
                log.error("Exception: " + e);
            } finally {
                group.shutdownGracefully();
            }
        });
        t.start();
    }

    public void sendMessage(AbstractMessage msg) {
        channel.writeAndFlush(msg);
    }

    public boolean isConnected() {
        return channel.isOpen();
    }

    public void closeConnection() {
        channel.close();
    }


}
