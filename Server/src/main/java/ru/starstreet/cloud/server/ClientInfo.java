package ru.starstreet.cloud.server;

import lombok.Data;

import java.nio.file.Path;

@Data
public class ClientInfo {
    private Path path;

    public ClientInfo(Path path) {
        this.path = path;
    }

    private int id = -1;
    public boolean isAuthorized() {
        return id != -1;
    }
}
