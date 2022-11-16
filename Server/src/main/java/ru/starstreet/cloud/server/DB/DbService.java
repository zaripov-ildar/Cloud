package ru.starstreet.cloud.server.DB;

import org.json.JSONObject;

import java.util.List;

public interface DbService {
    void init();
    void updateDirTree(int id, JSONObject dirTree);

    JSONObject getDirTree(int clientId);
    List<String> getSharedFiles(int clientId);

    boolean isValidLoginPass(String login, String password);

    int getIdByLogin(String token);

    String getLoginById(int id);

    void remove(String itemPath, int id);
}
