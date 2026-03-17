package com.project.cybershield.controllers;

import com.project.cybershield.entities.User;
import com.project.cybershield.exceptions.AuthenticationException;
import com.project.cybershield.repository.UserRepo;
import com.project.cybershield.services.LoginService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

/**
 * LOGIN CONTROLLER za JavaFX
 */
public class LoginController {

    private static final String REG_FILE = "/com/project/cybershield/ui/registration-page.fxml";

    private static final Logger logger =
            LoggerFactory.getLogger(LoginController.class);

    private LoginService loginService;

    private static User currentUser = null;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;



    public LoginController() {
    }

    public LoginController(UserRepo userRepo) {
        this.loginService = new LoginService(userRepo);
    }

    @FXML
    public void initialize() {
        logger.debug("LoginController initialized");

        // privremeno ako nema DI
        if (loginService == null) {
            loginService = new LoginService(new UserRepo());
        }
    }

    @FXML
    private void onLogin() {

        String username = usernameField.getText();
        String password = passwordField.getText();

        logger.info("[UI] Login attempt user={} server={}", username);

        try {

            User user = login(username, password);

            showAlert("Success", "Connection established.");

            logger.info("[UI] Login success for {}", username);

            // ovdje ide load dashboard scene

        } catch (AuthenticationException e) {

            showAlert("Login Failed", e.getMessage());

        }
    }

    @FXML
    private void onForgotPassword() {

        logger.info("Forgot password clicked");

        showAlert("Info", "Password recovery is not implemented yet.");
    }

    @FXML
    private void onGoRegister() throws IOException {

        logger.info("Navigate to register screen");
        goToRegister();
    }

    private void goToRegister() throws IOException {
        try{
            Stage stage = (Stage) usernameField.getScene().getWindow();

            stage.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(REG_FILE));
            stage.getScene().setRoot(loader.load());
        }catch (IOException ex){
            logger.error("Failed to load registration screen", ex);
            throw new IOException("Failed to lad reg screen");
        }
    }

    public User login(String username, String password)
            throws AuthenticationException {

        logger.info("[CONTROLLER] Login attempt for username: {}", username);

        try {

            username = sanitizeUsername(username);

            if (username == null || username.isBlank()) {
                throw new AuthenticationException("Username cannot be empty");
            }

            if (password == null || password.isBlank()) {
                throw new AuthenticationException("Password cannot be empty");
            }

            User authenticatedUser = loginService.login(username, password);

            currentUser = authenticatedUser;

            logger.info("[CONTROLLER] Login successful for: {}", username);

            return authenticatedUser;

        } catch (AuthenticationException e) {

            logger.warn("[CONTROLLER] Authentication failed: {}", e.getMessage());
            throw e;

        } catch (SQLException e) {

            logger.error("[CONTROLLER] Database error during login", e);
            throw new AuthenticationException("Database error. Please try again later.");

        } catch (IOException e) {

            logger.error("[CONTROLLER] IO error during login", e);
            throw new AuthenticationException("Server error. Please contact support.");

        } catch (Exception e) {

            logger.error("[CONTROLLER] Unexpected error during login", e);
            throw new AuthenticationException("Unexpected error occurred.");
        }
    }

    public void logout() {

        if (currentUser != null) {
            logger.info("[CONTROLLER] User logged out: {}", currentUser.getUsername());
            currentUser = null;
        }
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void requireLogin() throws AuthenticationException {

        if (!isLoggedIn()) {
            throw new AuthenticationException(
                    "You must be logged in to access this feature");
        }
    }

    // ---------- UTILS ----------

    private String sanitizeUsername(String username) {

        if (username == null) return null;

        username = username.trim();
        username = username.replaceAll("[^a-zA-Z0-9_]", "");

        return username;
    }

    private void showAlert(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}