package ru.starstreet.cloud.server.DB;


import java.util.List;

public interface DBService {
    boolean isValidPass(String login, String password);

    String getSharedFilesAsString(String argument);

    void share(String ownerLogin, String recipient, String path);

    void remove(List<String> deletedFileList);

    void removeRecipient(String recipient, String path);
}
