package ru.starstreet.cloud.server.DB.PojoDb;

import lombok.Data;
import ru.starstreet.cloud.server.DB.interfaces.AbstractDB;
import ru.starstreet.cloud.server.DB.SharedFilesUsers;
import ru.starstreet.cloud.server.DB.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class PojoDB implements AbstractDB {
    private Map<Integer, User> users;
    private Map<Integer, String> sharedFiles;
    private List<SharedFilesUsers> sfu;

    public PojoDB() {
        users = Map.of(
                1, new User(1, "Picard", "123".hashCode()),
                2, new User(2, "Kirk", "456".hashCode()),
                3, new User(3, "Archer", "789".hashCode())
        );
        sharedFiles = new HashMap<>();
        sfu = new ArrayList<>();
    }

    @Override
    public int getSharedFileId(String path) {
        for (Map.Entry<Integer, String> entry : sharedFiles.entrySet()) {
            if (entry.getValue().equals(path)) {
                return entry.getKey();
            }
        }
        return -1;
    }

    @Override
    public List<String> getSharedFiles(String recipientLogin) {
        int userId = getUserId(recipientLogin);
        return sfu.stream()
                .filter(sharedFilesUsers -> sharedFilesUsers.getUser_id() == userId)
                .map(sharedFilesUsers -> sharedFiles.get(sharedFilesUsers.getFile_id()))
                .toList();
    }

    @Override
    public int getUserId(String login) {
        for (Map.Entry<Integer, User> entry : users.entrySet()) {
            if (entry.getValue().getLogin().equals(login)) {
                return entry.getKey();
            }
        }
        return -1;
    }

    @Override
    public void share(int ownerId, int recipientId, String path) {
//        transaction
        sharedFiles.put(sharedFiles.size(), path);
        int fileId = getSharedFileId(path);
        sfu.add(new SharedFilesUsers(recipientId, ownerId, fileId));
//        commit
    }

    @Override
    public void removeSharedFile(int id) {
        sharedFiles.remove(id);
        sfu = sfu.stream()
                .filter(sharedFilesUsers -> sharedFilesUsers.getOwner_id() != id)
                .toList();
    }

    @Override
    public User getUserByLogin(String login) {
        int userId = getUserId(login);
        return users.get(userId);
    }

    @Override
    public void removeSharedFile(String fileName) {
        int fileId = getSharedFileId(fileName);
        sharedFiles.remove(fileId);
        sfu = sfu.stream()
                .filter(sharedFilesUsers -> sharedFilesUsers.getFile_id() != fileId)
                .toList();
    }

    @Override
    public void close() {

    }

    @Override
    public void updateFileName(String oldName, String newName) {
        int id = getSharedFileId(oldName);
        if (id != -1) {
            sharedFiles.put(id, newName);
        } else {
            sharedFiles.put(sharedFiles.size(), newName);
        }
    }

    @Override
    public void removeRecipient(int fileId, int userId) {
        sfu = sfu.stream()
                .filter(sharedFilesUsers -> sharedFilesUsers.getUser_id() != userId &&
                        sharedFilesUsers.getFile_id() != fileId)
                .toList();
        if (getSharedFiles(fileId).isEmpty()) {
            removeSharedFile(fileId);
        }
    }

    private List<String> getSharedFiles(int fileId) {
        return sfu.stream()
                .filter(sharedFilesUsers -> sharedFilesUsers.getFile_id() == fileId)
                .map(sharedFilesUsers -> sharedFiles.get(sharedFilesUsers.getFile_id()))
                .toList();
    }
}
