package ru.starstreet.cloud.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import ru.starstreet.cloud.core.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static ru.starstreet.cloud.core.Utils.HelpfulMethods.*;

@Slf4j
public class ClientController implements Initializable {

    public static final String SHARED_FILES = "Shared files";
    public static final String STORAGE = "Storage/";
    private boolean authorized;
    private Path currentClientPath;
    private Path currentServerPath;
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
    private String rootName;
    private String authStatus;
    private Path ROOT;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentClientPath = Path.of(System.getProperty("user.home")).toAbsolutePath();
        ROOT = File.listRoots()[0].toPath();
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

    public String getAuthStatus() {
        return authStatus;
    }


    public void setRooName(String rooName) {
        this.rootName = rooName;
    }

    private void getOnMsgReceived(AbstractMessage abstractMessage) {
        if (abstractMessage instanceof StringMessage msg) {
            Command cmd = msg.getCmd();
            String argument = msg.getArgument();

            switch (cmd) {
                case AUTH -> authStatus = argument;
                case PASSED -> {
                    currentServerPath = Path.of(msg.getArgument());
                    authorized = true;
                    refreshServerView();
                }
                case FILE_LIST, SHARED_FILES -> {
                    JSONArray arr = new JSONArray(argument);
                    List<String> files = new ArrayList<>();
                    for (Object o : arr) {
                        files.add((String) o);
                    }
                    updateView(serverFileList, files, serverPathLabel, currentServerPath);
                }
                case RENAME -> showAlert(argument);
                case PROPERTIES -> showReport(argument, "File attributes");
                case TRANSFER -> sendChunk(argument, this::sendMessage);
            }

        } else if (abstractMessage instanceof Chunk chunk) {

            receiveChunk(chunk, this::refreshClientView, this::sendMessage);
        }
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
        if (list == null) return new ArrayList<>();
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


    private Path react(ListView<String> listView, Path path) {
        String selected = listView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (selected.equals(ROOT.toString())) {
                return ROOT;
            }
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
            if (!path.toString().equals(ROOT.toString()) && path.getFileName().toString().equals(rootName)) {
                listView.getItems().add("Shared files/");
            }
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
        if (Path.of(rootName).resolve("Shared files/").toString().equals(currentServerPath.toString())) {

            client.sendMessage(new StringMessage(Command.SHARED_FILES, rootName));
        } else {
            client.sendMessage(new StringMessage(Command.FILE_LIST, currentServerPath.toString()));
        }
    }

    @FXML
    private void upload(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
            upload();
            refreshServerView();
        }
    }

    private void upload() {
        String fileName = clientFileList.getSelectionModel().getSelectedItem();
        Path departure = currentClientPath.resolve(fileName);
        if (!Files.exists(departure)) {
            showAlert(departure + " File not exist!!!");
        } else if (Files.isDirectory(departure)) {
            showAlert("Sorry. I can't send directories yet((");
        } else {
            Path destination = currentServerPath.resolve(departure.getFileName());

            sendPackage(departure.toString(), Path.of(STORAGE).resolve(destination).toAbsolutePath() + "/");
        }
    }

    private void sendPackage(String departure, String destination) {
        sendChunk(departure + "#" + destination + "#0", this::sendMessage);
    }


    @FXML
    private void createFolderOnServer(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
            if (currentServerPath.startsWith(rootName + "/Shared files")) {
                showAlert("You can't create folder here!");
                return;
            }
            String name = getNewFolderName();
            if (name != null) {
                String newPath = currentServerPath.resolve(name) + "/";
                client.sendMessage(new StringMessage(Command.CREATE_DIR, newPath));
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
        if (newName == null) {
            return null;
        }
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
        Platform.runLater(() -> {
            final Alert alert = new Alert(Alert.AlertType.ERROR, contentText,
                    new ButtonType("I got it", ButtonBar.ButtonData.CANCEL_CLOSE));
            alert.setTitle("Something goes wrong");
            alert.showAndWait();
        });
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
            List<String> deletedFileList = new ArrayList<>();
            recursiveRemoving(removingFile.toFile(), deletedFileList);
            showReport(listToString(deletedFileList), "Deleted files:");
            refreshClientView();
        }
    }

    private String listToString(List<String> list) {
        StringBuilder sb = new StringBuilder();
        list.forEach(f -> sb.append(f).append("\n"));
        return sb.toString();
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
        refreshServerView();
    }

    private void removeOnServer() {
        String name = serverFileList.getSelectionModel().getSelectedItem();
        if (askAlert("Do you really want removing " + name + "?")) {
            if (currentServerPath.toString().equals(rootName + "/" + SHARED_FILES)) {
                sendMessage(new StringMessage(Command.REMOVE_SHARED, rootName + " " + name));
            } else {
                name = currentServerPath + "/" + name;
                sendMessage(new StringMessage(Command.REMOVE, name));
            }
        }
    }

    @FXML
    private void download(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
            try {
                download();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    private void download() throws IOException {
        File departure;
        File destination;
        String name = serverFileList.getSelectionModel().getSelectedItem();
        if (name.endsWith("/")) {
            showAlert("Sorry. I can't download directories yet((");
        } else {
            if (currentServerPath.startsWith(rootName + "/" + SHARED_FILES)) {
                departure = new File(STORAGE + name);
                Path file = Path.of(name).getFileName();
                destination = currentClientPath.resolve(file).toFile();
            } else {
                departure = new File(STORAGE + currentServerPath + "/" + name);
                destination = currentClientPath.resolve(name).toFile();
            }
            sendMessage(new StringMessage(Command.TRANSFER, departure + "#" + destination + "#0"));
        }
    }

    @FXML
    private void cm_send() {
        upload();
        refreshServerView();
    }

    @FXML
    private void cm_remove() {
        removeOnClient();

    }

    @FXML
    private void sm_download() {
        try {
            download();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    @FXML
    private void sm_remove() {
        removeOnServer();
        refreshServerView();
    }

    public void renameOnClient() {
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

    public void cm_properties() {
        String selected = clientFileList.getSelectionModel().getSelectedItem();
        Path path = currentClientPath.resolve(selected).toAbsolutePath();
        try {
            showReport(getAttributes(path), "File attributes");
        } catch (IOException e) {
            showAlert("Can't read file attributes: \n" + e);
        }
    }

    private void showReport(String contentText, String title) {
        Platform.runLater(() -> {
            final Alert alert = new Alert(Alert.AlertType.INFORMATION, contentText,
                    new ButtonType("I got it", ButtonBar.ButtonData.CANCEL_CLOSE));
            alert.setTitle(title);
            alert.showAndWait();
        });
    }

    @FXML
    private void sm_rename() {
        String file = serverFileList.getSelectionModel().getSelectedItem();
        File oldName = currentServerPath.resolve(file).toFile();
        String newName = getTextFromUser("Rename", "Enter new name", "New name");
        if (newName != null) {
            if (oldName.toString().startsWith(rootName + "/" + SHARED_FILES)) {
                showAlert("You have no rights to rename this");
            } else {
                File newFile = currentServerPath.resolve(newName).toFile();
                sendMessage(new StringMessage(Command.RENAME, oldName + "#" + newFile));
            }
        }
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public void share() {
        String login = getTextFromUser("Share", "Input user login to share", "User login");
        if (login == null) return;
        String fileName = serverFileList.getSelectionModel().getSelectedItem();
        Path path = currentServerPath.resolve(fileName);
        sendMessage(new StringMessage(Command.SHARE, rootName + " " + login + " " + path));
    }

    public void sm_properties() {
        String file = serverFileList.getSelectionModel().getSelectedItem();
        if (currentServerPath.toString().startsWith(rootName + "/" + SHARED_FILES)) {
            sendMessage(new StringMessage(Command.PROPERTIES, file));
        } else {
            sendMessage(new StringMessage(Command.PROPERTIES, currentServerPath.resolve(file).toString()));
        }
    }

    public void sendingFiles() {
        Platform.runLater(() -> showReport(getSendingFiles(), "Sending files"));
    }

}
