package ru.starstreet.cloud.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import ru.starstreet.cloud.core.*;
import ru.starstreet.cloud.server.DB.DBService;
import ru.starstreet.cloud.server.DB.TempDataBaseService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static ru.starstreet.cloud.core.Utils.HelpfulMethods.*;

@Slf4j
public class PackedFileHandler extends SimpleChannelInboundHandler<AbstractMessage> {
    private final DBService service;
    private final Path STORAGE = Path.of("Storage");

    public PackedFileHandler(TempDataBaseService service) {
        this.service = service;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("client connected");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("client disconnected");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractMessage message) {
        if (message instanceof PackedFileMessage pf) {
            Path path = STORAGE.resolve(pf.getPath());
            try {
                Files.write(path, pf.getBytes());
                ctx.writeAndFlush(new StringMessage(Command.FILE_LIST, getFilesAsString(path)));
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        } else if (message instanceof StringMessage msg) {
            Command cmd = msg.getCmd();
            String argument = msg.getArgument();
            Path currentPath = STORAGE.resolve(argument);
            System.out.println(cmd + ">>>" + argument);

            switch (cmd) {
                case AUTH -> {
                    String[] pair = msg.getArgument().split(" ");
                    if (service.isValidPass(pair[0], pair[1])) {
                        currentPath = STORAGE.resolve(pair[0]);
                        createDirIfNotExists(currentPath);
                        ctx.writeAndFlush(new StringMessage(Command.PASSED, pair[0]));
                    } else {
                        ctx.writeAndFlush(new StringMessage(Command.AUTH, "Wrong login/password pair"));

                    }
                }
                case FILE_LIST -> sendFileList(ctx, currentPath);
                case DOWNLOAD -> sendFile(ctx, currentPath);
                case REMOVE -> {
                    List<String> deletedFileList = new ArrayList<>();
                    recursiveRemoving(currentPath.toFile(), deletedFileList);
                    service.remove(deletedFileList);

                }
                case CREATE_DIR -> {
                    createDirIfNotExists(currentPath);
                    sendFileList(ctx, currentPath.getParent());
                }
                case SHARED_FILES -> {
                    String stringFromFileArray = service.getSharedFilesAsString(argument);
                    ctx.writeAndFlush(new StringMessage(Command.SHARED_FILES, stringFromFileArray));
                }
                case SHARE -> {
                    String[] args = argument.split(" ", 3);
                    String ownerLogin = args[0];
                    String recipient = args[1];
                    String path = args[2];
                    service.share(ownerLogin, recipient, path);
                }
                case REMOVE_SHARED -> {
                    System.out.println(argument);
                    String[] args = argument.split(" ", 2);
                    String recipient = args[0];
                    String path = args[1];
                    service.removeRecipient(recipient, path);
                }
            }
        }
    }

    private void sendFile(ChannelHandlerContext ctx, Path path) {
        try {
            byte[] bytes = Files.readAllBytes(path);
            ctx.writeAndFlush(new PackedFileMessage(path.getFileName().toString(), bytes));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void sendFileList(ChannelHandlerContext ctx, Path path) {
        ctx.writeAndFlush(new StringMessage(Command.FILE_LIST, getFilesAsString(path)));
    }

    private void createDirIfNotExists(Path path) {
        if (Files.notExists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }
}
