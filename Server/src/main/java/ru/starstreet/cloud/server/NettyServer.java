package ru.starstreet.cloud.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;
import ru.starstreet.cloud.server.DB.TempDataBaseService;

import java.nio.file.Path;

@Slf4j
public class NettyServer {
    private static final int PORT = 8189;
    private static TempDataBaseService service = new TempDataBaseService();

    public static void main(String[] args) {
        EventLoopGroup auth = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();
        try  {
            ServerBootstrap bootstrap = new ServerBootstrap();
            ChannelFuture future = bootstrap.group(auth, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) {
                            channel.pipeline().addLast(
                                    new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                    new ObjectEncoder(),
                                    new PackedFileHandler(service)
                            );
                        }
                    })

                    .bind(PORT)
                    .sync();
            log.debug("Server Started");
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("exception: " + e);
        } finally {
            auth.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}

