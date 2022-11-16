package ru.starstreet.cloud.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;


public class ClientStarter extends Application {
    private ClientController controller;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ClientStarter.class.getResource("FXML/clientForm.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        configure(stage);

        controller = fxmlLoader.getController();
        stage.setScene(scene);
        authorize(stage);

        if (controller.isAuthorized()) {
            stage.show();
        }
        stage.setOnCloseRequest((event) -> controller.close());
    }

    private void configure(Stage stage) {
        stage.setTitle("Cloud storage");
        InputStream is = ClassLoader.getSystemResourceAsStream("logo.png");
        if (is != null) {
            stage.getIcons().add(new Image(is));
        }
        stage.setMinHeight(450);
        stage.setMinWidth(600);
    }

    private void authorize(Stage parent) throws IOException {
        Stage authStage = new Stage();
        FXMLLoader loader = new FXMLLoader(
                AuthorizationController.class.getResource("FXML/AuthorizationForm.fxml"));
        Scene authScene = new Scene(loader.load());
        authStage.setScene(authScene);
        authStage.setResizable(false);
        authStage.setTitle("Authorization");
        authStage.initModality(Modality.APPLICATION_MODAL);
        authStage.initOwner(parent.getScene().getWindow());
        AuthorizationController authController = loader.getController();
        authController.setMainController(controller);
        authStage.showAndWait();

    }

    public static void main(String[] args) {
        launch();
    }
}