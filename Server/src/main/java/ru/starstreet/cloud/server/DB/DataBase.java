package ru.starstreet.cloud.server.DB;

import lombok.Data;
import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class DataBase {
    private Map<Integer, User> users;
    private Map<Integer, String> sharedFiles;
    private List<SharedFilesUsers> sfu;

    public DataBase() {
        users = Map.of(
                1, new User(1, "Picard", "123"),
                2, new User(2, "Kirk", "456"),
                3, new User(3, "Archer", "789")
        );
        sharedFiles = new HashMap<>();
        sfu = new ArrayList<>();
    }

    public String getSharedFilesAsString(String arg) {
        int userId = getUserId(arg);
        JSONArray jArr = new JSONArray();
        System.out.println(sharedFiles);
        sfu.stream()
                .filter(sf -> sf.getUser_id() == userId)
                .forEach(sf -> {
                    System.out.println(sfu);
                    String file = sharedFiles.get(sf.getFile_id());
                    if (file != null) {
                        jArr.put(file);
                    }
                });
        return jArr.toString();
    }

    private int getSharedFileID(String path) {
        for (Map.Entry<Integer, String> entry : sharedFiles.entrySet()) {
            if (entry.getValue().equals(path)) {
                return entry.getKey();
            }
        }
        return -1;
    }

    public int getUserId(String login) {
        for (Map.Entry<Integer, User> entry : users.entrySet()) {
            if (entry.getValue().getLogin().equals(login)) {
                return entry.getKey();
            }
        }
        return -1;
    }


    public int addSharedFile(String path) {
        sharedFiles.put(sharedFiles.size(), path);
        for (Map.Entry<Integer, String> entry : sharedFiles.entrySet()) {
            if (entry.getValue().equals(path)) return entry.getKey();
        }

        return -1;
    }

    public void addSFU(int ownerId, int recipientId, int fileId) {
        sfu.add(new SharedFilesUsers(recipientId, ownerId, fileId));
        System.out.println("added:");
        System.out.println("\t" + sfu);
        System.out.println("\t" + sharedFiles);
    }

    public int getFileId(String df) {
        for (Map.Entry<Integer, String> entry : sharedFiles.entrySet()) {
            if (entry.getValue().equals(df)) {
                return entry.getKey();
            }
        }
        return -1;
    }

    public void removeSharedFile(int id) {
        sharedFiles.remove(id);
        sfu = sfu.stream()
                .filter(sharedFilesUsers -> sharedFilesUsers.getOwner_id() != id)
                .toList();
    }

    public void removeRecipient(int fileId, int userId) {
        sfu = sfu.stream()
                .filter(sharedFilesUsers -> sharedFilesUsers.getUser_id() != userId &&
                        sharedFilesUsers.getFile_id() !=fileId)
                .toList();
    }
}
