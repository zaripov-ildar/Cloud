package ru.starstreet.cloud.server.DB.PojoDb;

import org.json.JSONArray;
import ru.starstreet.cloud.server.DB.interfaces.DBService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PojoDBService implements DBService {
    private final PojoDB pojoDb;
    private final Set<String> clientsOnLine;


    public PojoDBService() {
        clientsOnLine = new HashSet<>();
        pojoDb = new PojoDB();
    }

    @Override
    public synchronized boolean isValidPass(String login, int passHash) {
        return pojoDb.getUserByLogin(login).getPassHash() == passHash;
    }


    @Override
    public synchronized String getSharedFilesAsString(String login) {
        JSONArray arr = new JSONArray(pojoDb.getSharedFiles(login));
        return arr.toString();
    }

    @Override
    public synchronized void share(String owner, String recipient, String path) {
        int ownerId = pojoDb.getUserId(owner);
        int recipientId = pojoDb.getUserId(recipient);
        pojoDb.share(ownerId, recipientId, path);
    }

    @Override
    public synchronized void remove(List<String> deletedFileList) {
        deletedFileList
                .forEach(pojoDb::removeSharedFile);
    }

    @Override
    public synchronized void removeRecipient(String recipient, String path) {
        int fileId = pojoDb.getSharedFileId(path);
        int userId = pojoDb.getUserId(recipient);
        pojoDb.removeRecipient(fileId, userId);
    }

    @Override
    public void close() {
        System.out.println("Good bye!");
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
        pojoDb.updateFileName(oldName, newName);
    }

}
