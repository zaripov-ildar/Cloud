package ru.starstreet.cloud.core.Utils;

import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HelpfulMethods {
    public static List<String> recursiveRemoving(File removingFile) {
        List<String> result = new ArrayList<>();
        recursiveRemoving(removingFile, result);
        return result;

    }

    public static void recursiveRemoving(File removingFile, List<String> list) {
        File[] deletingItems = removingFile.listFiles();
        if (deletingItems != null) {
            for (File file : deletingItems) {
                recursiveRemoving(file);
            }
        }
        list.add(removingFile.getName());
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

    public static String getFilesAsString(Path path) {
        File[] fileArr = path.toFile().listFiles();
        if (fileArr == null) return new JSONArray().toString();
        return getStringFromFileArray(fileArr);
    }

    public static String getStringFromFileArray(File[] fileArr) {
        JSONArray arr = new JSONArray();
        for (File file : fileArr) {
            arr.put(file.getName() + (file.isDirectory() ? "/" : ""));
        }
        return arr.toString();
    }

}
