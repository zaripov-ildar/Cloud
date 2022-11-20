package ru.starstreet.cloud.server.DB.H2Db;

import lombok.extern.slf4j.Slf4j;
import ru.starstreet.cloud.server.DB.interfaces.AbstractDB;
import ru.starstreet.cloud.server.DB.User;

import java.io.Closeable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class H2DB implements Closeable, AbstractDB {
    private Connection connection;
    private Statement statement;

    public H2DB() {
        try {
            Class.forName("org.h2.Driver");
            this.connection = DriverManager.getConnection("jdbc:h2:mem:CloudDB");
            this.statement = connection.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            log.error(e.getMessage());
        }
        generateData();
    }

    private void generateData() {
        try {
            generate();
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    private void generate() throws SQLException {
        String query = "CREATE TABLE clients (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "login VARCHAR(30), " +
                "passHash INT);"
                +
                "CREATE TABLE sharedFiles (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "fileName VARCHAR(255));"
                +
                "CREATE TABLE sharedFilesUsers (" +
                "recipient_id INT," +
                "owner_id INT," +
                "file_id INT," +
                "FOREIGN KEY (recipient_id) REFERENCES clients(id) ON DELETE CASCADE," +
                "FOREIGN KEY (owner_id) REFERENCES clients(id) ON DELETE CASCADE," +
                "FOREIGN KEY (file_id) REFERENCES sharedFiles(id) ON DELETE CASCADE);";
        statement.executeUpdate(query);


        String userData = String.format("INSERT INTO clients(login, passHash) " +
                "VALUES ( 'Picard', %d)," +
                "('Kirk', %d)," +
                "('Archer', %d);", "123".hashCode(), "456".hashCode(), "789".hashCode());
        statement.executeUpdate(userData);
    }

    @Override
    public void close() {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                log.error(e.getMessage());
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                log.error(e.getMessage());
            }
        }
    }

    @Override
    public void updateFileName(String oldName, String newName) {
        String query = "UPDATE sharedFiles " +
                "SET fileName = '" + newName + "'" +
                "WHERE fileName = '" + oldName + "'";
        try {
            statement.executeUpdate(query);
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public List<String> getSharedFiles(String recipientLogin) {
        String query = "SELECT fileName FROM clients c " +
                "JOIN sharedFilesUsers sfu ON c.id = sfu.recipient_id " +
                "JOIN sharedFiles sf ON sf.id = sfu.file_id " +
                "WHERE login ='" + recipientLogin + "';";
        List<String> result = new ArrayList<>();
        try {
            ResultSet set = statement.executeQuery(query);
            while (set.next()) {
                result.add(set.getString(1));
            }

        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return result;
    }

    @Override
    public int getUserId(String login) {
        String query = "SELECT id FROM clients WHERE login = '" + login + "';";
        try {
            ResultSet set = statement.executeQuery(query);
            if (set.next()) {
                return set.getInt(1);
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return -1;
    }

    @Override
    public void share(int ownerId, int recipientId, String path) {
        String query =
                "INSERT INTO sharedFiles (fileName)" +
                        "VALUES ('" + path + "');";
        try {
            statement.executeUpdate(query);
            int fileId = getSharedFileId(path);
            query = String.format("" +
                    "INSERT INTO sharedFilesUsers (recipient_id, owner_id, file_id)" +
                    "VALUES(%d, %d, %d)", recipientId, ownerId, fileId);
            statement.executeUpdate(query);
        } catch (SQLException e) {
            log.error(e.getMessage());
        }

    }

    @Override
    public int getSharedFileId(String path) {
        try {
            ResultSet set = statement.executeQuery("" +
                    "SELECT id FROM sharedFiles WHERE filename ='" + path + "';");
            if (set.next()) {
                return set.getInt(1);
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return -1;
    }

    @Override
    public void removeRecipient(int fileId, int recipientId) {
        String query = String.format("" +
                "DELETE FROM sharedFilesUsers " +
                "WHERE file_id = %d AND recipient_id = %d", fileId, recipientId);
        try {
            statement.executeUpdate(query);
            ResultSet set = statement.executeQuery("" +
                    "SELECT * FROM sharedFilesUsers " +
                    "WHERE file_id =" + fileId);
            int size = set.getFetchSize();
            if (size == 0) {
                removeSharedFile(fileId);
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public void removeSharedFile(int id) {
        String query = "DELETE FROM sharedFiles " +
                "WHERE id = " + id;
        try {
            statement.executeUpdate(query);
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public User getUserByLogin(String login) {
        try {
            ResultSet set = statement.executeQuery("SELECT * FROM clients " +
                    "WHERE login = '" + login + "';");
            if (set.next()) {
                return new User(set.getInt(1), login, set.getInt(3));
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @Override
    public void removeSharedFile(String fileName) {
        try {
            statement.executeUpdate("DELETE FROM sharedFiles " +
                    "WHERE fileName = '" + fileName + "';");
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }
}
