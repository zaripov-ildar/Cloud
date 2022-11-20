package ru.starstreet.cloud.client;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ru.starstreet.cloud.core.Command;
import ru.starstreet.cloud.core.StringMessage;

import java.util.concurrent.TimeUnit;

public class AuthorizationController {
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;
    @FXML
    private Button authBtn;
    private ClientController controller;

    public void authorize() throws InterruptedException {
        String login = loginField.getText();
        controller.setRooName(login);
        int password = passwordField.getText().hashCode();
        controller.sendMessage(new StringMessage(Command.AUTH, login + " " + password));

        TimeUnit.MILLISECONDS.sleep(500);

        if (controller.isConnected() && controller.isAuthorized()) {
            errorLabel.setVisible(true);
            Stage stage = (Stage) authBtn.getScene().getWindow();
            stage.close();
        } else if (controller.isConnected() && !controller.isAuthorized()) {
            errorLabel.setVisible(true);
            errorLabel.setText(controller.getAuthStatus());
        } else {
            errorLabel.setVisible(true);
            errorLabel.setText("Can't find server");
        }
    }

    public void setMainController(ClientController controller) {
        this.controller = controller;
    }
}
