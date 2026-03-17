package com.project.cybershield.ui;

import com.project.cybershield.enums.SpeedLimit;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * JAVAFX DOWNLOAD PROGRESS POPUP
 *
 * Prikazuje:
 * - Progress bar s postotkom
 * - Brzinu preuzimanja
 * - Preuzeto / Ukupno
 * - Selector za ograničenje brzine
 * - Status poruke
 */
public class DownloadProgressPopup {

    private Stage stage;
    private ProgressBar progressBar;
    private Label lblStatus;
    private Label lblSpeed;
    private Label lblDownloaded;
    private Label lblPercentage;
    private ComboBox<SpeedLimit> cmbSpeedLimit;
    private Button btnCancel;

    private boolean cancelled = false;
    private SpeedLimitChangeListener speedLimitListener;

    /**
     * Konstruktor - Kreira popup prozor
     */
    public DownloadProgressPopup() {
        createUI();
    }

    /**
     * Kreira UI komponente
     */
    private void createUI() {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle("Threat Intelligence Download");
        stage.setResizable(false);

        // Main container
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1a2332;");
        root.setPrefWidth(500);

        // ═══════════════════════════════════════════════
        // HEADER - Title
        // ═══════════════════════════════════════════════
        Label lblTitle = new Label("Downloading Threat Intelligence");
        lblTitle.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: white;"
        );

        // ═══════════════════════════════════════════════
        // STATUS LABEL
        // ═══════════════════════════════════════════════
        lblStatus = new Label("Preparing download...");
        lblStatus.setStyle(
                "-fx-font-size: 13px; " +
                        "-fx-text-fill: #94a3b8;"
        );

        // ═══════════════════════════════════════════════
        // PROGRESS BAR
        // ═══════════════════════════════════════════════
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(460);
        progressBar.setPrefHeight(30);
        progressBar.setStyle(
                "-fx-accent: #135bec; " +
                        "-fx-background-color: #0d1117; " +
                        "-fx-background-radius: 6;"
        );

        // Percentage label on progress bar
        lblPercentage = new Label("0%");
        lblPercentage.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: white;"
        );

        StackPane progressContainer = new StackPane(progressBar, lblPercentage);

        // ═══════════════════════════════════════════════
        // DOWNLOAD INFO (Downloaded / Total)
        // ═══════════════════════════════════════════════
        lblDownloaded = new Label("0 KB / 0 KB");
        lblDownloaded.setStyle(
                "-fx-font-size: 12px; " +
                        "-fx-text-fill: #cbd5e1; " +
                        "-fx-font-family: 'Courier New', monospace;"
        );

        // ═══════════════════════════════════════════════
        // SPEED LABEL
        // ═══════════════════════════════════════════════
        lblSpeed = new Label("Speed: 0 KB/s");
        lblSpeed.setStyle(
                "-fx-font-size: 12px; " +
                        "-fx-text-fill: #10b981; " +
                        "-fx-font-family: 'Courier New', monospace;"
        );

        // Info container
        HBox infoBox = new HBox(20);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.getChildren().addAll(lblDownloaded, lblSpeed);

        // ═══════════════════════════════════════════════
        // SPEED LIMIT SELECTOR
        // ═══════════════════════════════════════════════
        Label lblSpeedLimit = new Label("Speed Limit:");
        lblSpeedLimit.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px;");

        cmbSpeedLimit = new ComboBox<>();
        cmbSpeedLimit.getItems().addAll(SpeedLimit.values());
        cmbSpeedLimit.setValue(SpeedLimit.MEDIUM);
        cmbSpeedLimit.setPrefWidth(150);
        cmbSpeedLimit.setStyle(
                "-fx-background-color: #2a364d; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 6;"
        );

        cmbSpeedLimit.setOnAction(e -> {
            if (speedLimitListener != null) {
                speedLimitListener.onSpeedLimitChanged(cmbSpeedLimit.getValue());
            }
        });

        HBox speedLimitBox = new HBox(10);
        speedLimitBox.setAlignment(Pos.CENTER);
        speedLimitBox.getChildren().addAll(lblSpeedLimit, cmbSpeedLimit);

        // ═══════════════════════════════════════════════
        // SEPARATOR
        // ═══════════════════════════════════════════════
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #2a364d;");

        // ═══════════════════════════════════════════════
        // CANCEL BUTTON
        // ═══════════════════════════════════════════════
        btnCancel = new Button("Cancel Download");
        btnCancel.setPrefWidth(150);
        btnCancel.setStyle(
                "-fx-background-color: #ef4444; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;"
        );

        btnCancel.setOnMouseEntered(e ->
                btnCancel.setStyle(
                        "-fx-background-color: #dc2626; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-weight: bold; " +
                                "-fx-background-radius: 6; " +
                                "-fx-cursor: hand;"
                )
        );

        btnCancel.setOnMouseExited(e ->
                btnCancel.setStyle(
                        "-fx-background-color: #ef4444; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-weight: bold; " +
                                "-fx-background-radius: 6; " +
                                "-fx-cursor: hand;"
                )
        );

        btnCancel.setOnAction(e -> {
            cancelled = true;
            close();
        });

        // ═══════════════════════════════════════════════
        // LAYOUT ASSEMBLY
        // ═══════════════════════════════════════════════
        root.getChildren().addAll(
                lblTitle,
                lblStatus,
                progressContainer,
                infoBox,
                separator,
                speedLimitBox,
                btnCancel
        );

        Scene scene = new Scene(root);
        stage.setScene(scene);

        // Prevent closing with X button (must use Cancel)
        stage.setOnCloseRequest(e -> {
            e.consume();
            cancelled = true;
            close();
        });
    }

    /**
     * Prikaži popup
     */
    public void show() {
        Platform.runLater(() -> stage.show());
    }

    /**
     * Zatvori popup
     */
    public void close() {
        Platform.runLater(() -> stage.close());
    }

    /**
     * AŽURIRAJ NAPREDAK (thread-safe)
     *
     * @param description Opis (npr. "Emerging Threats IPs")
     * @param downloaded Preuzeto bytea
     * @param total Ukupno bytea
     * @param percentage Postotak (0-100)
     * @param currentSpeed Trenutna brzina (bytes/s)
     */
    public void updateProgress(String description, long downloaded, long total,
                               double percentage, long currentSpeed) {
        Platform.runLater(() -> {
            // Update progress bar
            progressBar.setProgress(percentage / 100.0);

            // Update percentage label
            lblPercentage.setText(String.format("%.1f%%", percentage));

            // Update status
            lblStatus.setText("Downloading: " + description);

            // Update downloaded info
            lblDownloaded.setText(String.format("%s / %s",
                    formatBytes(downloaded),
                    formatBytes(total)
            ));

            // Update speed
            lblSpeed.setText(String.format("Speed: %s/s", formatBytes(currentSpeed)));
        });
    }

    /**
     * Postavi status poruku
     */
    public void setStatus(String status) {
        Platform.runLater(() -> lblStatus.setText(status));
    }

    /**
     * Završi preuzimanje (uspješno)
     */
    public void setComplete(String message) {
        Platform.runLater(() -> {
            progressBar.setProgress(1.0);
            lblPercentage.setText("100%");
            lblStatus.setText("✓ " + message);
            lblStatus.setStyle("-fx-font-size: 13px; -fx-text-fill: #10b981;");

            btnCancel.setText("Close");
            btnCancel.setStyle(
                    "-fx-background-color: #10b981; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-radius: 6; " +
                            "-fx-cursor: hand;"
            );
        });
    }

    /**
     * Postavi grešku
     */
    public void setError(String errorMessage) {
        Platform.runLater(() -> {
            lblStatus.setText("✗ Error: " + errorMessage);
            lblStatus.setStyle("-fx-font-size: 13px; -fx-text-fill: #ef4444;");

            btnCancel.setText("Close");
        });
    }

    /**
     * Je li preuzimanje otkazano?
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Postavi listener za promjenu brzine
     */
    public void setSpeedLimitChangeListener(SpeedLimitChangeListener listener) {
        this.speedLimitListener = listener;
    }

    /**
     * Postavi inicijalno ograničenje brzine
     */
    public void setInitialSpeedLimit(SpeedLimit speedLimit) {
        Platform.runLater(() -> cmbSpeedLimit.setValue(speedLimit));
    }

    /**
     * Format bytes to human-readable
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    /**
     * Listener interface za promjenu brzine
     */
    public interface SpeedLimitChangeListener {
        void onSpeedLimitChanged(SpeedLimit newLimit);
    }
}