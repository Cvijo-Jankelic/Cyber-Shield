package com.project.cybershield;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CyberShieldApplication extends Application {
    private static final Logger logger = LoggerFactory.getLogger(CyberShieldApplication.class);

    private static final String INITIAL_VIEW = "/com/project/cybershield/ui/login-page.fxml";
    private static final double INITIAL_WIDTH = 1200;
    private static final double INITIAL_HEIGHT = 800;

    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            Scene scene = createScene(INITIAL_VIEW);
            primaryStage.setTitle("CyberShield");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(960);
            primaryStage.setMinHeight(640);
            primaryStage.show();
            logger.info("CyberShield started with initial view: {}", INITIAL_VIEW);
        } catch (IOException e) {
            logger.error("Error loading initial FXML view: {}", INITIAL_VIEW, e);
            throw e;
        }
    }

    private Scene createScene(String fxmlPath) throws IOException {
        var resource = CyberShieldApplication.class.getResource(fxmlPath);
        if (resource == null) {
            throw new IOException("FXML resource not found: " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(resource);
        Parent root = loader.load();
        return new Scene(root, INITIAL_WIDTH, INITIAL_HEIGHT);
    }

    @Override
    public void stop() throws Exception {
        logger.info("CyberShield shutting down...");
        super.stop();
    }

}
