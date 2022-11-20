package ru.starstreet.cloud.server.DB;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SharedFilesUsers {
    private int user_id;
    private int owner_id;
    private int file_id;
}
