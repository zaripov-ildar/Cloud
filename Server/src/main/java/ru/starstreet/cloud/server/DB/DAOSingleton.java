package ru.starstreet.cloud.server.DB;

import org.json.JSONObject;
import ru.starstreet.cloud.core.JSONNavigator;

import java.nio.file.Path;
import java.util.List;

public enum DAOSingleton implements DbService {

    INSTANCE() {
        @Override
        public void init() {
        }

        @Override
        public void updateDirTree(int id, JSONObject dirTree) {
            db.users.get(id).setDirTree(dirTree);
        }

        @Override
        public JSONObject getDirTree(int clientId) {
            return db.users.get(clientId).getDirTree();
        }

        @Override
        public List<String> getSharedFiles(int clientId) {
            return null;
        }

        @Override
        public boolean isValidLoginPass(String login, String password) {
            User user = getUserByLogin(login);
            if (user != null) {
                return user.getPassword().equals(password);
            }
            return false;
        }

        @Override
        public int getIdByLogin(String login) {
            for (User user : db.users.values()) {
                if (user.getLogin().equals(login)) {
                    return user.getId();
                }
            }
            return -1;
        }

        @Override
        public String getLoginById(int id) {
            return db.users.get(id).getLogin();
        }

        @Override
        public void remove(String itemPath, int id) {
            JSONObject object = db.users.get(id).getDirTree();
            JSONNavigator navigator = new JSONNavigator(object);
            navigator.remove(itemPath);
            db.users.get(id).setDirTree(navigator.getJsonObject());
        }


        private User getUserByLogin(String login) {
            for (User user : db.users.values()) {
                if (user.getLogin().equals(login)) {
                    return user;
                }
            }
            return null;
        }

    };
    private static final DataBase db = new DataBase();

    public void addNewFolder(Path path, int id) {
        addItem(path, id, "/");
    }

    public void addNewFile(Path path, int id) {
        addItem(path, id, "");
    }

    public void addItem(Path path, int id, String suffix) {
        JSONObject object = db.users.get(id).getDirTree();
        JSONNavigator nav = new JSONNavigator(object);
        nav.addToFolder(path.getParent(), path.getFileName().toString() + suffix);
        db.users.get(id).setDirTree(nav.getJsonObject());
    }
}
