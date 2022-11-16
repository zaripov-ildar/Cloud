package ru.starstreet.cloud.server.DB;

public interface DBService {
    boolean isValidPass(String login, String password);
}
