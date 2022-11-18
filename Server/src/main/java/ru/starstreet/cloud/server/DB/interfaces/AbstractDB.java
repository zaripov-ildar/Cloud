package ru.starstreet.cloud.server.DB.interfaces;

import ru.starstreet.cloud.server.DB.User;

import java.sql.SQLException;
import java.util.List;

public interface AbstractDB {
    List<String> getSharedFiles(String recipientLogin);
    int getUserId(String login);
    void share(int ownerId, int recipientId, String path);
    int getSharedFileId(String path);
    void removeRecipient(int fileId, int userId);
    void removeSharedFile(int id);
    User getUserByLogin(String login);
    void removeSharedFile(String fileName);
    void close();

    void updateFileName(String oldName, String newName);
}
