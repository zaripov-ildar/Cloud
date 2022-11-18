package ru.starstreet.cloud.server.DB.interfaces;


import java.util.List;

public interface DBService{
    boolean isValidPass(String login, int password);

    String getSharedFilesAsString(String argument);

    void share(String ownerLogin, String recipient, String path);

    void remove(List<String> deletedFileList);

    void removeRecipient(String recipient, String path);


    void close();

    void clientLeaved(String login);

    boolean isLogged(String s);

    void addLogin(String login);

    void renameIfShared(String oldName, String newName);
}
