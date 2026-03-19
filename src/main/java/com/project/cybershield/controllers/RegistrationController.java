package com.project.cybershield.controllers;

import com.project.cybershield.entities.User;
import com.project.cybershield.enums.Role;
import com.project.cybershield.exceptions.RegistrationException;
import com.project.cybershield.repository.UserRepo;
import com.project.cybershield.services.RegisterService;
import com.project.cybershield.util.ViewNavigator;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

public class RegistrationController {
    private static final String LOGIN_FILE = "/com/project/cybershield/ui/login-page.fxml";
    private static final UserRepo userRepo = new UserRepo();

    private static final Logger logger =
            LoggerFactory.getLogger(RegistrationController.class);

    private RegisterService registerService = new RegisterService(userRepo);

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;


    public RegistrationController() {
    }

    public RegistrationController(RegisterService registerService) {
        this.registerService = registerService;
    }

    @FXML
    private void onActivate() {
        String username = fullNameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        logger.info("[REGISTRATION] UI registration attempt: {}", email);

        try {
            register(username, password, confirmPassword, Role.ANALYST);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Account successfully registered. Please log in.");
            logger.info("[UI] Registration successful for {}", email);
            goToLogin();
        } catch (RegistrationException e) {
            showAlert(Alert.AlertType.ERROR, "Registration failed", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Unexpected error occurred.");
        }
    }

    @FXML
    private void onBackToLogin() {
        logger.info("User clicked back to login");
        goToLogin();
    }

    public User register(String username, String password, String confirmPassword, Role role) throws RegistrationException {
        logger.info("[REGISTRATION] Registration attempt for user: {}", username);

        try {

            username = sanitizeUsername(username);

            User registeredUser = registerService.register(username, password, confirmPassword, role);
            logger.info("[CONTROLLER] Registration successful for: {}", username);

            return registeredUser;

        } catch (RegistrationException e) {

            logger.warn("[CONTROLLER] Registration failed: {}", e.getMessage());
            throw e;

        } catch (SQLException e) {

            logger.error("[CONTROLLER] Database error during registration", e);
            throw new RegistrationException("Database error. Please try again later.");

        } catch (IOException e) {

            logger.error("[CONTROLLER] IO error during registration", e);
            throw new RegistrationException("Server error. Please contact support.");

        } catch (Exception e) {

            logger.error("[CONTROLLER] Unexpected error during registration", e);
            throw new RegistrationException("Unexpected error occurred.");
        }
    }

    private String sanitizeUsername(String username) {

        if (username == null) return null;

        username = username.trim();
        username = username.replaceAll("[^a-zA-Z0-9_]", "");

        return username;
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void goToLogin() {
        try {
            ViewNavigator.switchScene(fullNameField, LOGIN_FILE);
        } catch (IOException e) {
            logger.error("Failed to load login screen", e);
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Unable to open the login screen.");
        }
    }
}
