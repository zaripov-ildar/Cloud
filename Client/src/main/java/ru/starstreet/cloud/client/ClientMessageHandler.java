package ru.starstreet.cloud.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.starstreet.cloud.core.AbstractMessage;
import ru.starstreet.cloud.core.OnMessageReceived;

public class ClientMessageHandler extends SimpleChannelInboundHandler<AbstractMessage> {
    private final OnMessageReceived callback;

    public ClientMessageHandler(OnMessageReceived callback) {
        this.callback = callback;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractMessage msg) {
        callback.onReceive(msg);
    }
}
