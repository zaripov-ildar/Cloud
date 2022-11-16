package ru.starstreet.cloud.server.DB;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataBase {
    //    users table^
    public Map<Integer, User> users;

    //    files table (pk path string, fk user_id integer ):
    Map<String, Integer> storedFiles;

    //    table shared (fk user_id int, fk file_id int)
    Map<Integer, List<String>> sharedFiles;


    public DataBase() {

        users = new HashMap<>();
        String login = "Picard";
        users.put(1, new User(1, getRoot(login), login, "123"));
        login = "Janeway";
        users.put(2, new User(2, getRoot(login), login, "456"));
        login = "Pike";
        users.put(3, new User(3, getRoot(login), login, "789"));
        login = "Kirk";
        users.put(4, new User(4, getRoot(login), login, "147"));

        sharedFiles = new HashMap<>();
        storedFiles = new HashMap<>();
    }

    private JSONObject getRoot(String login) {
        return new JSONObject("{\n" +
                "\t\"name\": \"" + login + "\",\n" +
                "\t\"" + login + "/\": [\"Shared Files/\"],\n" +
                "\t\"" + login + "/Shared Files/\": []\n" +
                "}");
    }
}
