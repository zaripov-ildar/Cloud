package ru.starstreet.cloud.core;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class WorkStatistic {
    private static final List<File> fileList = new ArrayList<>();
    private static final List<String> fileExtension = new ArrayList<>(
            List.of(
                    "java",
                    "css",
                    "fxml"
            )
    );
    private static String text;

    public static void main(String[] args) {
        recursiveSearch("./");
        int files = fileList.size();
        filesToText();
        int chars = text.length();
        int strings = countSymbol("\\n");
        int words = countSymbol(" ");
        System.out.println("Статистика по проекту:");
        System.out.println("\t файлов: " + files);
        System.out.println("\t строк: " + strings);
        System.out.println("\t \"слов\": " + words);
        System.out.println("\t символов: " + chars);
    }

    private static int countSymbol(String s) {
        return text.split(s).length - 1;
    }

    private static void filesToText() {
        StringBuilder sb = new StringBuilder();
        fileList.forEach(file -> {
            try {
                String fileText = Files.readString(Path.of(file.getPath()), StandardCharsets.UTF_8);
                sb.append(fileText);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        text = sb.toString();
    }

    private static void recursiveSearch(String path) {
        File dir = new File(path);
        File[] arrFiles = dir.listFiles();
        if (arrFiles != null) {
            for (File arrFile : arrFiles) {
                if (arrFile.isFile()) {
                    String[] fExtensions = arrFile.getName().split("\\.");
                    String fExt = fExtensions[fExtensions.length - 1];
                    if (fileExtension.contains(fExt)) {
                        fileList.add(arrFile);
                    }
                } else {
                    recursiveSearch(arrFile.getPath());
                }
            }
        }
    }
}
