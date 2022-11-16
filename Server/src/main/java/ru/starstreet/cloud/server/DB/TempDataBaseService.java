package ru.starstreet.cloud.server.DB;

public class TempDataBaseService implements DBService{
    private final DataBase db;

    public TempDataBaseService() {
        db = new DataBase();
    }

    @Override
    public boolean isValidPass(String login, String password) {
        for (User value : db.getUsers().values()) {
            if (value.getLogin().equals(login) && value.getPassword().equals(password)){
                return true;
            }
        }
        return false;
    }
}
