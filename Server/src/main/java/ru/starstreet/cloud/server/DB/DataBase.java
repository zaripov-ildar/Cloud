package ru.starstreet.cloud.server.DB;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class DataBase {
    private Map<Integer, User> users;
    private Map<Integer, String> sharedFiles;
    private Map<Integer, SharedFilesUsers> sfu;

    public DataBase() {
        users = Map.of(
                1, new User(1, "Picard", "123"),
                2, new User(2, "Kirk", "456"),
                3, new User(3, "Archer", "789")
        );
        sharedFiles = new HashMap<>();
        sfu = new HashMap<>();
    }
}
