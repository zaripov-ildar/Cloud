package ru.starstreet.cloud.server.DB.H2Db;

import org.json.JSONArray;
import ru.starstreet.cloud.server.DB.interfaces.AbstractDB;
import ru.starstreet.cloud.server.DB.interfaces.DBService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class H2DbService implements DBService {
    private final Set<String> clientsOnLine;
    private final AbstractDB db;

    public H2DbService() {
        clientsOnLine = new HashSet<>();
        this.db = new H2DB();
    }

    @Override
    public boolean isValidPass(String login, int password) {
        return db.getUserByLogin(login).getPassHash() == password;
    }

    @Override
    public String getSharedFilesAsString(String recipientLogin) {
        JSONArray arr = new JSONArray(db.getSharedFiles(recipientLogin));
        return arr.toString();
    }

    @Override
    public void share(String ownerLogin, String recipient, String path) {
        System.out.println(db.getUserId(ownerLogin));
        System.out.println(db.getUserId(recipient));
        System.out.println(path);
        db.share(db.getUserId(ownerLogin),
                db.getUserId(recipient),
                path);
    }

    @Override
    public void remove(List<String> deletedFileList) {
        deletedFileList
                .forEach(db::removeSharedFile);
    }

    @Override
    public void removeRecipient(String recipient, String path) {
        int fileId = db.getSharedFileId(path);
        int userId = db.getUserId(recipient);
        db.removeRecipient(fileId, userId);
    }


    @Override
    public void close() {
        db.close();
    }

    @Override
    public void clientLeaved(String login) {
        clientsOnLine.remove(login);
    }

    @Override
    public boolean isLogged(String login) {
        return clientsOnLine.contains(login);
    }

    @Override
    public void addLogin(String login) {
        clientsOnLine.add(login);
    }

    @Override
    public void renameIfShared(String oldName, String newName) {
        db.updateFileName(oldName, newName);
    }
}
