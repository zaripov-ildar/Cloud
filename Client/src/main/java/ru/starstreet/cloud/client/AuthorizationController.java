package ru.starstreet.cloud.client;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ru.starstreet.cloud.core.Message;

import java.util.concurrent.TimeUnit;

public class AuthorizationController {
    //    TODO расставить анотации и сделать приватными
    public TextField loginField;
    public PasswordField passwordField;
    public Label errorLabel;
    public Button authBtn;
    private ClientController controller;

    public void authorize(ActionEvent actionEvent) throws InterruptedException {
        String login = loginField.getText();
        String password = passwordField.getText();
        controller.sendMessage(new Message(login + " " + password));
        TimeUnit.MILLISECONDS.sleep(500);
        if (controller.isConnected() && controller.isAuthorized()){
            errorLabel.setVisible(true);
            errorLabel.setText("Connecting...");
            Stage stage = (Stage)authBtn.getScene().getWindow();
            stage.close();
        } else if (controller.isConnected() && !controller.isAuthorized()){
            errorLabel.setVisible(true);
            errorLabel.setText("Wrong Login/Password");
        } else{
            errorLabel.setVisible(true);
            errorLabel.setText("Can't find server");
        }
    }

    public void setMainController(ClientController controller) {

        this.controller = controller;
    }
}
