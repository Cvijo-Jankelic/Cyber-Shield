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

    private static final String FXML_FILE = "/com/project/cybershield/ui/registration-page.fxml";

    @Override
    public void start(Stage primaryStage) throws IOException {
        try{
            FXMLLoader loader = new FXMLLoader(CyberShieldApplication.class.getResource(FXML_FILE));
            Parent root = loader.load();

            Scene scene = new Scene(root);

            primaryStage.setTitle("CyberShield");
            primaryStage.setScene(scene);
            primaryStage.show();
            System.out.println("[APP] CyberShield started - Auth screen loaded");

        }catch (IOException e){
            logger.error("Error loading FXML file", e);
            logger.info("Error loading FXML file");
            System.exit(1);
        }

    }

    @Override
    public void stop() throws Exception {
        System.out.println("[APP] CyberShield shutting down...");
        super.stop();
    }

}
