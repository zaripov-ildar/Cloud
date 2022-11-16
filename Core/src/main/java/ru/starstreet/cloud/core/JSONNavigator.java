package ru.starstreet.cloud.core;

import lombok.Data;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Data
public class JSONNavigator {
    private final JSONObject jsonObject;
    private final String rootName;

    public JSONNavigator(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
        rootName = jsonObject.getString("name");
    }

    public List<String> getFileList(Path path) {
        List<String> result = new ArrayList<>();
        for (Object o : (JSONArray) jsonObject.get(path.toString() + "/")) {
            result.add(o.toString());
        }
        return result;
    }

    public void addToFolder(Path parent, String fileOrFolder) {
        System.out.println(jsonObject);
        JSONArray arr = (JSONArray) jsonObject.get(parent.toString() + "/");
        arr.put(fileOrFolder);
        if (fileOrFolder.endsWith("/")) {
            jsonObject.put(parent.resolve(fileOrFolder) + "/", new JSONArray());
        }
    }


    public void remove(String itemPath) {
        removeFromJSONArray(itemPath);
        jsonObject.remove(itemPath);
    }


    private void removeFromJSONArray(String itemPath) {
        Path path = Path.of(itemPath);
        String fileName = path.getFileName().toString()
                + (itemPath.endsWith("/") ? "/" : "");
        Path parent = path.getParent();
        if (parent != null) {
            String parentStr = parent.toString();
            String key = parentStr + "/";
            JSONArray arr = jsonObject.getJSONArray(key);
            List<String> list = arr.toList()
                    .stream()
                    .map(o -> (String) o)
                    .filter(s -> {
                        System.out.println(s + ">>>" + fileName);
                        return !s.equals(fileName);
                    })
                    .toList();
            jsonObject.put(key, list);
        }
    }
}
