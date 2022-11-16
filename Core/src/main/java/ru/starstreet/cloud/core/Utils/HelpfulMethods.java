package ru.starstreet.cloud.core.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class HelpfulMethods {
    public static void recursiveRemoving(File removingFile) {
        File[] deletingItems = removingFile.listFiles();
        if (deletingItems != null) {
            for (File file : deletingItems) {
                recursiveRemoving(file);
            }
        }
        removingFile.delete();
    }

    private static String formatDateTime(FileTime fileTime) {

        LocalDateTime localDateTime = fileTime
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        return localDateTime.format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    public static String getAttributes(Path path) throws IOException {
        BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
        return attr.isDirectory() ? "Folder" : "File" +
                " name: " + path.getFileName() +
                "\nCreated: " + formatDateTime(attr.creationTime()) +
                "\nlastModifiedTime: " + formatDateTime(attr.lastModifiedTime()) +
                "\nLastAccessTime: " + formatDateTime(attr.lastAccessTime());
    }
}
