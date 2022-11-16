package ru.starstreet.cloud.server.DB;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import ru.starstreet.cloud.core.Message;
import ru.starstreet.cloud.server.ClientInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class AuthorizationService {
    public static void authorize(String message, ChannelHandlerContext channelHandlerContext, ClientInfo client) {
        String[] tokens = message.split(" ");
        String login = tokens[0];
        String password = tokens[1];
        if (tokens.length != 2) {
            channelHandlerContext.writeAndFlush("That was not a couple login & pass");
        }
        DAOSingleton dao = DAOSingleton.INSTANCE;
        if (dao.isValidLoginPass(login, password)) {
            client.setId(dao.getIdByLogin(login));
            channelHandlerContext.writeAndFlush(new Message("passed"));

            String hierarchy = dao.getDirTree(client.getId()).toString();
            channelHandlerContext.writeAndFlush(new Message(hierarchy));
            Path path = client.getPath().resolve(login);
            client.setPath(path);
            if (!Files.exists(path)) {
                try {
                    Files.createDirectory(path);
                    path = path.resolve("Shared Files");
                    if (!Files.exists(path)) {
                        Files.createDirectory(path);
                    }
                } catch (IOException e) {
                    log.error("Error: " + e);
                }
            }
        }
    }
}
