package com.project.cybershield.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public final class ViewNavigator {
    private ViewNavigator() {
    }

    public static void switchScene(Node currentNode, String fxmlPath) throws IOException {
        URL resource = ViewNavigator.class.getResource(fxmlPath);
        if (resource == null) {
            throw new IOException("FXML resource not found: " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(resource);
        Parent root = loader.load();

        Stage stage = (Stage) currentNode.getScene().getWindow();
        Scene scene = stage.getScene();
        if (scene == null) {
            stage.setScene(new Scene(root));
            return;
        }

        scene.setRoot(root);
        stage.sizeToScene();
    }
}
