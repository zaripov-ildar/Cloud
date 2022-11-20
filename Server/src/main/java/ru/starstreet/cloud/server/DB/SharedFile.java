package ru.starstreet.cloud.server.DB;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SharedFile {
    private int id;
    private String path;
}
