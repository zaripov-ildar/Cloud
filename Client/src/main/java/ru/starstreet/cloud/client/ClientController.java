package ru.starstreet.cloud.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import ru.starstreet.cloud.core.AbstractMessage;
import ru.starstreet.cloud.core.JSONNavigator;
import ru.starstreet.cloud.core.Message;
import ru.starstreet.cloud.core.PackedFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static ru.starstreet.cloud.core.Utils.HelpfulMethods.getAttributes;
import static ru.starstreet.cloud.core.Utils.HelpfulMethods.recursiveRemoving;

@Slf4j
public class ClientController implements Initializable {
    private static boolean isAuthorized;
    private Path currentClientPath;
    private Path currentServerPath;

    private JSONNavigator navigator;
    private NettyClient client;
    @FXML
    private Label serverPathLabel;
    @FXML
    private Label clientPathLabel;
    @FXML
    private ListView<String> clientFileList;
    @FXML
    private ListView<String> serverFileList;
    @FXML
    private ContextMenu clientMenu;
    @FXML
    private ContextMenu serverMenu;

    private void getOnMsgReceived(AbstractMessage abstractMessage) {
        if (abstractMessage instanceof Message) {
            String message = ((Message) abstractMessage).getMessage();
            System.out.println(message);
            if (message.equals("passed")) {
                isAuthorized = true;
            } else if (message.startsWith("{")) {
                updateNavigator(new JSONObject(message));
                refreshServerView();
            }
        } else if (abstractMessage instanceof PackedFile pf) {
            String fileName = pf.getPath();
            Path file = currentClientPath.resolve(fileName);
            try {
                Files.write(file, pf.getBytes());
                refreshClientView();
            } catch (IOException e) {
                log.error("Error: " + e);
            }
        }
    }

    private void updateNavigator(JSONObject jsonObject) {
        if (navigator == null) {
            navigator = new JSONNavigator(jsonObject);
            currentServerPath = Path.of(navigator.getRootName());
        } else {
            navigator = new JSONNavigator(jsonObject);
        }
    }


    public boolean isAuthorized() {
        return isAuthorized;
    }

    public void sendMessage(AbstractMessage message) {
        client.sendMessage(message);
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    public void close() {
        client.closeConnection();
    }

    @FXML
    private void clientViewDoubleClick(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            currentClientPath = react(clientFileList, currentClientPath);
            refreshClientView();
        }
        if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 1) {
            clientMenu.hide();
        }
    }

    @FXML
    private void serverViewDoubleClick(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            currentServerPath = react(serverFileList, currentServerPath);
            refreshServerView();
        }
        if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 1) {
            serverMenu.hide();
        }
    }


    private List<String> sortFileList(List<String> list) {
        list.sort((f1, f2) -> {
            if (f1.endsWith("/") == f2.endsWith("/")) {
                return f1.compareTo(f2);
            } else if (!f1.endsWith("/") && f2.endsWith("/")) {
                return 1;
            } else {
                return -1;
            }
        });
        return list;

    }

    private List<String> getFileList(Path p) {
        File[] list = p.toFile().listFiles();
        return Arrays.stream(list)
                .filter(File::canRead)
                .map(f -> {
                    if (f.isDirectory()) {
                        return f.getName() + "/";
                    }
                    return f.getName();
                })
                .collect(Collectors.toList());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentClientPath = Path.of(System.getProperty("user.home"));
        client = new NettyClient(this::getOnMsgReceived);
        refreshClientView();
        clientFileList.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                clientMenu.show(clientFileList, event.getScreenX(), event.getScreenY());
            }
        });
        serverFileList.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                serverMenu.show(clientFileList, event.getScreenX(), event.getScreenY());
            }
        });
    }

    private Path react(ListView<String> listView, Path path) {
        String selected = listView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (selected.equals("../")) {
                Path temp = path.getParent();
                path = temp == null ? path : temp;
            } else if (selected.endsWith("/")) {
                path = path.resolve(selected.split("/")[0]);
            }
        }
        return path;
    }

    private void updateView(ListView<String> listView, List<String> list, Label label, Path path) {
        Platform.runLater(() -> {
            label.setText(path.toString());
            listView.getItems().clear();
            listView.getItems().add("../");
            listView.getItems().addAll(list);
        });
    }

    private void refreshClientView() {
        updateView(clientFileList,
                sortFileList(getFileList(currentClientPath)),
                clientPathLabel,
                currentClientPath);
    }

    private void refreshServerView() {
        updateView(serverFileList,
                sortFileList(navigator.getFileList(currentServerPath)),
                serverPathLabel,
                currentServerPath);
    }

    private static String cutLabelName(String labelName) {
        if (labelName.length() > 17) {
            return "..." + labelName.substring(labelName.length() - 17);
        }
        return labelName;
    }

    @FXML
    private void sendPackage(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
            sendPackage();
        }
    }

    private void sendPackage() {
        String fileName = clientFileList.getSelectionModel().getSelectedItem();
        Path sendingFile = currentClientPath.resolve(fileName);
        if (!Files.exists(sendingFile)) {
            showAlert(sendingFile + " File not exist!!!");
        } else if (Files.isDirectory(sendingFile)) {
            showAlert("Sorry. I can't send directories yet((");
        } else {
            try {
                byte[] arr = Files.readAllBytes(sendingFile);
                Path pathOnServer = currentServerPath.resolve(sendingFile.getFileName());
                PackedFile pf = new PackedFile(pathOnServer.toString(), arr);
                client.sendMessage(pf);
            } catch (IOException e) {
                log.error("Error: " + e);
            }
        }
    }

    @FXML
    private void createFolderOnServer(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
            String name = getNewFolderName();
            if (name != null) {
                String newPath = currentServerPath.resolve(name) + "/";
                System.out.println("create on server: " + newPath);
                client.sendMessage(new Message("CREATE_DIR " + newPath));
            }
        }
    }

    private String getNewFolderName() {
        return getNewItemName("folder");
    }

    private String getNewFileName() {
        return getNewItemName("file");
    }

    private String getNewItemName(String item) {
        String newName = getTextFromUser("Create new" + item,
                "Enter " + item + " name:", "New " + item);
        if (newName.length() > 255) {
            showAlert("Too long " + item + "filename!");
            return null;
        }
        Set<Character> forbiddenSymbols = Set.of('/', '\\', ':', '*', '?', '\"', '<', '>', '|', '+', '%', '!', '@');
        for (int i = 0; i < newName.length(); i++) {
            if (forbiddenSymbols.contains(newName.charAt(i))) {
                showAlert(item + " name can't contain: \"" + newName.charAt(i) + "\"");
                return null;
            }
        }
        return newName;
    }

    private String getTextFromUser(String title, String header, String field) {
        TextInputDialog dialog = new TextInputDialog(field);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private void showAlert(String contentText) {
        final Alert alert = new Alert(Alert.AlertType.ERROR, contentText,
                new ButtonType("I got it", ButtonBar.ButtonData.CANCEL_CLOSE));
        alert.setTitle("Something goes wrong");
        alert.showAndWait();
    }

    @FXML
    private void createFolderOnClient(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
            String name = getNewFolderName();
            if (name != null) {
                try {
                    Files.createDirectory(currentClientPath.resolve(name));
                    refreshClientView();
                } catch (IOException e) {
                    log.error("Error: " + e);
                }
            }
        }
    }

    @FXML
    private void createFileOnClient(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
            String name = getNewFileName();
            if (name != null) {
                try {
                    Files.createFile(currentClientPath.resolve(name));
                    refreshClientView();
                } catch (IOException e) {
                    log.error("Error: " + e);
                }
            }
        }
    }

    @FXML
    private void removeOnClient(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
            removeOnClient();
        }
    }

    private void removeOnClient() {
        String name = clientFileList.getSelectionModel().getSelectedItem();
        Path removingFile = currentClientPath.resolve(name);
        if (askAlert("Do you really want removing " + name + "?")) {
            recursiveRemoving(removingFile.toFile());
            refreshClientView();
        }
    }

    private boolean askAlert(String s) {
        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION, s,
                new ButtonType("Yes", ButtonBar.ButtonData.YES),
                new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE));
        alert.setTitle("Attention!");
        Optional<ButtonType> answer = alert.showAndWait();
        return !answer.map(buttonType ->
                buttonType.getButtonData().isCancelButton()).orElse(true);
    }

    @FXML
    private void removeOnServer(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
            removeOnServer();
        }
    }

    private void removeOnServer() {
        String name = serverFileList.getSelectionModel().getSelectedItem();
        name = currentServerPath + "/" + name;
        System.out.println(name);
        if (askAlert("Do you really want removing " + name + "?")) {
            sendMessage(new Message("REMOVE " + name));
        }
    }

    @FXML
    private void download(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
            download();
        }
    }

    private void download() {
        String name = serverFileList.getSelectionModel().getSelectedItem();
        if (name.endsWith("/")) {
            showAlert("Sorry. I can't download directories yet((");
        } else {
            sendMessage(new Message("DOWNLOAD " + currentServerPath + "/" + name));
        }
    }

    @FXML
    private void cm_send(ActionEvent event) {
        sendPackage();
    }

    @FXML
    private void cm_remove(ActionEvent event) {
        removeOnClient();
    }

    @FXML
    private void sm_download(ActionEvent event) {
        download();
    }

    @FXML
    private void sm_remove(ActionEvent event) {
        removeOnServer();
    }

    public void renameOnClient(ActionEvent event) {
        String file = clientFileList.getSelectionModel().getSelectedItem();
        File oldName = currentClientPath.resolve(file).toAbsolutePath().toFile();
        String newName = getTextFromUser("Rename", "Enter new name", "New name");
        if (newName != null) {

            File newFile = currentClientPath.resolve(newName).toAbsolutePath().toFile();
            if (newFile.exists()) {
                showAlert(newFile + " already exists!!!");
            } else {
                boolean result = oldName.renameTo(newFile);
                if (result) {
                    refreshClientView();
                } else {
                    showAlert("Couldn't rename: " + oldName + " to " + newFile);
                }
            }
        }
    }

    public void cm_attributes(ActionEvent event) {
        String selected = clientFileList.getSelectionModel().getSelectedItem();
        Path path = currentClientPath.resolve(selected).toAbsolutePath();
        try {
            showReport(getAttributes(path), "File attributes");
        } catch (IOException e) {
            showAlert("Can't read file attributes: \n" + e);
        }
    }
    private void showReport(String contentText, String title) {
        final Alert alert = new Alert(Alert.AlertType.INFORMATION, contentText,
                new ButtonType("I got it", ButtonBar.ButtonData.CANCEL_CLOSE));
        alert.setTitle(title);
        alert.showAndWait();
    }

    public void sm_rename(ActionEvent event) {
        //todo
    }
}
