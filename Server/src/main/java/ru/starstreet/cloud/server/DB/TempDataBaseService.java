package ru.starstreet.cloud.server.DB;

import java.util.List;

public class TempDataBaseService implements DBService {
    private final DataBase db;



    public TempDataBaseService() {
        db = new DataBase();
    }

    @Override
    public synchronized boolean isValidPass(String login, String password) {
        for (User value : db.getUsers().values()) {
            if (value.getLogin().equals(login) && value.getPassword().equals(password)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public synchronized String getSharedFilesAsString(String currentPath) {
        String[] args = currentPath.split("/Shared files");
        System.out.println("received login: " + args[0]);
        return db.getSharedFilesAsString(args[0]);
    }

    @Override
    public synchronized void share(String ownerLogin, String recipient, String path) {
        int ownerId = db.getUserId(ownerLogin);
        int recipientId = db.getUserId(recipient);
        //transaction
        int fileId = db.addSharedFile(path);
        db.addSFU(ownerId, recipientId, fileId);
        //commit
    }

    @Override
    public synchronized void remove(List<String> deletedFileList) {
        deletedFileList.stream()
                .map(db::getFileId)
                .forEach(db::removeSharedFile);
    }

    @Override
    public synchronized void removeRecipient(String recipient, String path) {
        int fileId = db.getFileId(path);
        int userId = db.getUserId(recipient);
        db.removeRecipient(fileId, userId);
    }
}
