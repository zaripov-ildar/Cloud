package ru.starstreet.cloud.server.DB;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.json.JSONObject;

@AllArgsConstructor
@Data
public class User {
    private int id;
    private JSONObject dirTree;
    private String login;
    private String password;
}
