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
import ru.starstreet.cloud.server.DB.interfaces.DBService;
import ru.starstreet.cloud.server.DB.H2Db.H2DbService;
import io.netty.handler.stream.ChunkedWriteHandler;

@Slf4j
public class Server {
    private static final int PORT = 8189;
    private static final DBService service = new H2DbService();

    public static void main(String[] args) {
        EventLoopGroup auth = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            ChannelFuture future = bootstrap.group(auth, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) {
                            channel.pipeline().addLast(
                                    new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                    new ObjectEncoder(),
                                    new PackedFileHandler(service),
                                    new ChunkedWriteHandler()
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

