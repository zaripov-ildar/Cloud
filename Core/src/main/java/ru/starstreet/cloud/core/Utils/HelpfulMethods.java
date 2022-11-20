package ru.starstreet.cloud.core.Utils;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import ru.starstreet.cloud.core.Chunk;
import ru.starstreet.cloud.core.Command;
import ru.starstreet.cloud.core.StringMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class HelpfulMethods {
    private static final Map<String, Float> sendingFiles = new HashMap<>();

    public static void recursiveRemoving(File removingFile, List<String> list) {
        File[] deletingItems = removingFile.listFiles();
        if (deletingItems != null) {
            for (File file : deletingItems) {
                recursiveRemoving(file, list);
            }
        }
        String fileName = removingFile.toString();
        if (fileName.startsWith("Storage/")) {
            fileName = fileName.replace("Storage/", "");
        }
        if (removingFile.delete()) {
            list.add(fileName);
        }
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
        long size = Files.size(path) >> 20;
        return attr.isDirectory() ? "Folder" : "File" +
                " name: " + path.getFileName() +
                "\n\nCreated: " + formatDateTime(attr.creationTime()) +
                "\n\nlastModifiedTime: " + formatDateTime(attr.lastModifiedTime()) +
                "\n\nLastAccessTime: " + formatDateTime(attr.lastAccessTime()) +
                "\n\nSize: " + size + "Mb";
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

    public static void sendBigFile(String argument, BigFileSender sender) {
        String[] args = argument.split("#");
        String departure = new File(args[0]).getAbsolutePath();
        String destination = args[1];

        long position = Long.parseLong(args[2]);
        try (RandomAccessFile file = new RandomAccessFile(new File(departure), "r")) {
            byte[] bytes = new byte[1 << 15];
            file.seek(position);
            int read = file.read(bytes);
            long size = file.length();
            sendingFiles.put(Path.of(destination).getFileName().toString(), (float) position / size);
            sender.sendFile(new Chunk(bytes, position, read, departure, destination, size));
        } catch (IOException e) {
            log.error(">>>" + e);
        }
    }

    public static void receiveBigFile(Chunk chunk, OnDownloadEnd notifier, MessageSender sender) {
        sendingFiles.put(chunk.getDestination(), (float) chunk.getPosition() / chunk.getSize());
        File destination = new File(chunk.getDestination());
        if (!destination.exists()) {
            try {
                Files.createFile(destination.toPath());
            } catch (IOException e) {
                log.error("Error: " + e);
            }
        }
        try (FileOutputStream fos = new FileOutputStream(destination, true)) {
            long start = chunk.getPosition();
            int bytesToRead = chunk.getBytesToRead();
            if (bytesToRead == -1) {
                notifier.refreshView();
                return;
            }
            fos.write(chunk.getBytes(), 0, bytesToRead);
            sender.send(new StringMessage(Command.TRANSFER, chunk.getDeparture() + "#"
                    + chunk.getDestination() + "#" + (bytesToRead + start + 1)));
        } catch (IOException e) {
            log.error(">>>" + e);
        }
    }

    public static String getSendingFiles() {
        StringBuilder sb = new StringBuilder();
        sendingFiles.values().removeIf(v -> v >= .9);
        if (sendingFiles.size() == 0) {
            return "No one file is sending";
        }
        for (String s : sendingFiles.keySet()) {
            sb.append(s)
                    .append(":\t")
                    .append(String.format("%.2f %%", 100 * sendingFiles.get(s)))
                    .append("\n");
        }
        return sb.toString();
    }
}
