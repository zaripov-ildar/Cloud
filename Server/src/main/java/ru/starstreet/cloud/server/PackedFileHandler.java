package ru.starstreet.cloud.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import ru.starstreet.cloud.core.AbstractMessage;
import ru.starstreet.cloud.core.Message;
import ru.starstreet.cloud.core.PackedFile;
import ru.starstreet.cloud.core.Utils.HelpfulMethods;
import ru.starstreet.cloud.server.DB.AuthorizationService;
import ru.starstreet.cloud.server.DB.DAOSingleton;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class PackedFileHandler extends SimpleChannelInboundHandler<AbstractMessage> {
    private final ClientInfo client;

    public PackedFileHandler(ClientInfo client) {
        this.client = client;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("client connected");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("client disconnected");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractMessage msg) {
        Path storage = Path.of("Storage");
        if (msg instanceof PackedFile pf) {
            Path path = storage.resolve(pf.getPath());
            try {
                Files.write(path, pf.getBytes());
//                todo remove duplication
                DAOSingleton.INSTANCE.addNewFile(Path.of(pf.getPath()), client.getId());
                ctx.writeAndFlush(new Message(DAOSingleton.INSTANCE.getDirTree(client.getId()).toString()));
            } catch (IOException e) {
                log.error("Error: " + e);
            }
        } else if (msg instanceof Message message) {
            if (!client.isAuthorized()) {
                AuthorizationService.authorize(message.getMessage(), ctx, client);
            } else {
                String[] tokens = message.getMessage().split(" ", 2);
                System.out.println("command: " + tokens[0]);
                System.out.println("argument: " + tokens[1]);
                Path path = storage.resolve(tokens[1]);
                switch (tokens[0]) {
                    case "CREATE_DIR" -> {
                        try {
                            if (Files.notExists(path)) {
                                Files.createDirectory(path);
                            }
//                todo remove duplication
                            DAOSingleton.INSTANCE.addNewFolder(Path.of(tokens[1]), client.getId());
                            ctx.writeAndFlush(new Message(DAOSingleton.INSTANCE.getDirTree(client.getId()).toString()));
                        } catch (IOException e) {
                            log.debug("Error: " + e);
                        }
                    }
                    case "CREATE_FILE" -> {
//                        todo
                    }
                    case "REMOVE" -> {
                        HelpfulMethods.recursiveRemoving(path.toFile());
//                todo remove duplication
                        DAOSingleton.INSTANCE.remove(tokens[1], client.getId());
                        ctx.writeAndFlush(new Message(DAOSingleton.INSTANCE.getDirTree(client.getId()).toString()));
                    }
                    case "DOWNLOAD" -> {
                        Path file = storage.resolve(tokens[1]);
                        try {
                            byte[] arr = Files.readAllBytes(file);
                            ctx.writeAndFlush(new PackedFile(file.getFileName().toString(), arr));
                        } catch (IOException e) {
                            log.debug("Error: " + e);
                        }
                    }
                }
            }
        }
    }


}
