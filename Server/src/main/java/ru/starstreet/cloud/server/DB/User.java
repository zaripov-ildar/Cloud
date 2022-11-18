package ru.starstreet.cloud.server.DB;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class User {
    private int id;
    private String login;
    private int passHash;
}
