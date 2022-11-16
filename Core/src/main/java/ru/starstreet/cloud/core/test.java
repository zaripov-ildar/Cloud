package ru.starstreet.cloud.core;


import java.nio.file.Path;

public class test {
    public static void main(String[] args) {
//        JSONObject object = new JSONObject("{\"name\":\"user\",\"/user\": []}");
//        JSONNavigator nav = new JSONNavigator(object);
//        nav.addToFolder("/user", "folder1/");
//        nav.addToFolder("/user/folder1", "file1.txt");
//        nav.addToFolder("/user", "file2.txt");
//        nav.addToFolder("/user", "folder2/");
//        System.out.println(nav.getFileList("/user"));
       String p = "p//t/";
       Path path = Path.of(p);
        System.out.println(path);


    }
}
