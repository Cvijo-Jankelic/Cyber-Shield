package com.project.cybershield.ui;

import com.project.cybershield.api.AbuseIPDBClient;
import com.project.cybershield.api.ThreatIntelligenceReport;
import com.project.cybershield.entities.Incident;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

public class IncidentDetailsWindow {

    private Stage stage;
    private Incident incident;
    private AbuseIPDBClient abuseIPDBClient;

    // UI komponente
    private Label lblThreatLevel;
    private Label lblAbuseScore;
    private ProgressIndicator loadingIndicator;
    private VBox contentContainer;

    // Formatteri
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Konstruktor
     */
    public IncidentDetailsWindow(Incident incident) {
        this.incident = incident;
        this.abuseIPDBClient = new AbuseIPDBClient();
        createUI();
    }

    /**
     * Konstruktor s custom API keyem
     */
    public IncidentDetailsWindow(Incident incident, String apiKey) {
        this.incident = incident;
        this.abuseIPDBClient = new AbuseIPDBClient(apiKey);
        createUI();
    }

    /**
     * Kreira UI
     */
    private void createUI() {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Incident Details - ID: " + incident.getId());
        stage.setWidth(700);
        stage.setHeight(650);

        // Main container
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #1a2332;");

        // HEADER - Incident Info (koristi tvoje podatke)
        VBox headerBox = createHeaderSection();

        // SEPARATOR
        Separator sep1 = new Separator();
        sep1.setStyle("-fx-background-color: #2a364d;");

        // THREAT INTELLIGENCE SECTION
        Label lblThreatTitle = new Label("🔍 Threat Intelligence (AbuseIPDB)");
        lblThreatTitle.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #135bec;"
        );

        // Loading indicator
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setPrefSize(50, 50);
        loadingIndicator.setStyle("-fx-progress-color: #135bec;");

        Label lblLoading = new Label("Fetching threat intelligence from AbuseIPDB REST API...");
        lblLoading.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        VBox loadingBox = new VBox(10, loadingIndicator, lblLoading);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(20));

        // Content container
        contentContainer = new VBox(10, loadingBox);

        // CLOSE BUTTON
        Button btnClose = new Button("Close");
        btnClose.setPrefWidth(100);
        btnClose.setStyle(
                "-fx-background-color: #2a364d; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;"
        );
        btnClose.setOnAction(e -> stage.close());

        HBox closeBox = new HBox(btnClose);
        closeBox.setAlignment(Pos.CENTER_RIGHT);

        // ASSEMBLY
        root.getChildren().addAll(
                headerBox,
                sep1,
                lblThreatTitle,
                contentContainer,
                closeBox
        );

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #1a2332; -fx-background-color: #1a2332;");

        Scene scene = new Scene(scrollPane);
        stage.setScene(scene);
    }

    /**
     * Kreira header sekciju - KORISTI TVOJE PODATKE
     */
    private VBox createHeaderSection() {
        VBox header = new VBox(8);

        // Title - možeš dodati lookup za attack type name ako imaš
        String attackTypeName = getAttackTypeName(incident.getAttackTypeId());

        Label lblTitle = new Label(attackTypeName);
        lblTitle.setStyle(
                "-fx-font-size: 18px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: white;"
        );

        // Severity badge (tvoj severity je int)
        Label lblSeverity = new Label(getSeverityName(incident.getSeverity()));
        lblSeverity.setPadding(new Insets(4, 12, 4, 12));
        lblSeverity.setStyle(
                "-fx-background-color: " + getSeverityColor(incident.getSeverity()) + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 12; " +
                        "-fx-font-size: 11px;"
        );

        // Info grid - TVOJI podaci
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(8);
        grid.setPadding(new Insets(10, 0, 0, 0));

        addInfoRow(grid, 0, "Incident ID:", String.valueOf(incident.getId()));
        addInfoRow(grid, 1, "Source IP:", incident.getSourceIP());
        addInfoRow(grid, 2, "Destination Port:", String.valueOf(incident.getDestinationPort()));
        addInfoRow(grid, 3, "Attack Time:",
                incident.getAttackTime().format(DATE_FORMATTER));

        if (incident.getNote() != null && !incident.getNote().isEmpty()) {
            addInfoRow(grid, 4, "Note:", incident.getNote());
        }

        header.getChildren().addAll(lblTitle, lblSeverity, grid);

        return header;
    }

    /**
     * Dodaje red u info grid
     */
    private void addInfoRow(GridPane grid, int row, String label, String value) {
        Label lblKey = new Label(label);
        lblKey.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        Label lblValue = new Label(value);
        lblValue.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");

        grid.add(lblKey, 0, row);
        grid.add(lblValue, 1, row);
    }

    /**
     * Kreira threat intelligence content nakon što se učita iz API-ja
     */
    private VBox createThreatIntelligenceContent(ThreatIntelligenceReport report) {
        VBox content = new VBox(15);

        // THREAT LEVEL BADGE
        HBox threatLevelBox = new HBox(10);
        threatLevelBox.setAlignment(Pos.CENTER_LEFT);

        Label lblThreatLevelTitle = new Label("Threat Level:");
        lblThreatLevelTitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");

        lblThreatLevel = new Label(report.threatLevel);
        lblThreatLevel.setPadding(new Insets(6, 16, 6, 16));
        lblThreatLevel.setStyle(
                "-fx-background-color: " + getThreatLevelColor(report.threatLevel) + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 15; " +
                        "-fx-font-size: 14px;"
        );

        threatLevelBox.getChildren().addAll(lblThreatLevelTitle, lblThreatLevel);

        // ABUSE SCORE (Large display)
        VBox scoreBox = new VBox(5);
        scoreBox.setAlignment(Pos.CENTER);
        scoreBox.setPadding(new Insets(15));
        scoreBox.setStyle(
                "-fx-background-color: #0d1117; " +
                        "-fx-background-radius: 10; " +
                        "-fx-border-color: #2a364d; " +
                        "-fx-border-radius: 10;"
        );

        Label lblScoreTitle = new Label("Abuse Confidence Score");
        lblScoreTitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        lblAbuseScore = new Label(report.abuseConfidenceScore + "%");
        lblAbuseScore.setStyle(
                "-fx-text-fill: " + getScoreColor(report.abuseConfidenceScore) + "; " +
                        "-fx-font-size: 36px; " +
                        "-fx-font-weight: bold;"
        );

        Label lblScoreDesc = new Label(report.getSummary());
        lblScoreDesc.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 11px;");
        lblScoreDesc.setWrapText(true);
        lblScoreDesc.setMaxWidth(400);
        lblScoreDesc.setAlignment(Pos.CENTER);

        scoreBox.getChildren().addAll(lblScoreTitle, lblAbuseScore, lblScoreDesc);

        // STATISTICS GRID
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(12);
        statsGrid.setPadding(new Insets(15));
        statsGrid.setStyle(
                "-fx-background-color: #0d1117; " +
                        "-fx-background-radius: 10;"
        );

        addStatRow(statsGrid, 0, "Total Reports:", String.valueOf(report.totalReports));
        addStatRow(statsGrid, 1, "Distinct Reporters:", String.valueOf(report.numDistinctUsers));
        addStatRow(statsGrid, 2, "Whitelisted:", report.isWhitelisted ? "Yes" : "No");

        if (report.lastReportedAt != null) {
            addStatRow(statsGrid, 3, "Last Reported:", report.lastReportedAt);
        }

        // LOCATION & ISP
        Label lblLocationTitle = new Label("📍 Location & ISP Information");
        lblLocationTitle.setStyle(
                "-fx-font-size: 13px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #cbd5e1;"
        );

        GridPane locationGrid = new GridPane();
        locationGrid.setHgap(20);
        locationGrid.setVgap(12);
        locationGrid.setPadding(new Insets(15));
        locationGrid.setStyle(
                "-fx-background-color: #0d1117; " +
                        "-fx-background-radius: 10;"
        );

        addStatRow(locationGrid, 0, "Country:",
                report.countryName != null ? report.countryName + " (" + report.countryCode + ")" : "Unknown");
        addStatRow(locationGrid, 1, "ISP:",
                report.isp != null ? report.isp : "Unknown");
        addStatRow(locationGrid, 2, "Domain:",
                report.domain != null ? report.domain : "Unknown");
        addStatRow(locationGrid, 3, "Usage Type:",
                report.usageType != null ? report.usageType : "Unknown");

        // ATTACK CATEGORIES
        if (report.attackCategories != null && !report.attackCategories.isEmpty()) {
            Label lblCategoriesTitle = new Label("⚠️ Reported Attack Categories");
            lblCategoriesTitle.setStyle(
                    "-fx-font-size: 13px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-text-fill: #cbd5e1;"
            );

            TextArea txtCategories = new TextArea();
            txtCategories.setEditable(false);
            txtCategories.setPrefRowCount(4);
            txtCategories.setWrapText(true);
            txtCategories.setStyle(
                    "-fx-control-inner-background: #0d1117; " +
                            "-fx-text-fill: #ef4444; " +
                            "-fx-font-size: 12px; " +
                            "-fx-font-family: 'Courier New', monospace;"
            );

            StringBuilder categories = new StringBuilder();
            for (String category : report.attackCategories) {
                categories.append("• ").append(category).append("\n");
            }
            txtCategories.setText(categories.toString());

            content.getChildren().addAll(lblCategoriesTitle, txtCategories);
        }

        // API INFO
        Label lblApiInfo = new Label(
                "Data provided by AbuseIPDB REST API\n" +
                        "https://api.abuseipdb.com/api/v2/check"
        );
        lblApiInfo.setStyle(
                "-fx-text-fill: #64748b; " +
                        "-fx-font-size: 10px; " +
                        "-fx-font-style: italic;"
        );
        lblApiInfo.setAlignment(Pos.CENTER);
        lblApiInfo.setMaxWidth(Double.MAX_VALUE);

        // ASSEMBLY
        content.getChildren().addAll(
                threatLevelBox,
                scoreBox,
                statsGrid,
                lblLocationTitle,
                locationGrid
        );

        if (report.attackCategories != null && !report.attackCategories.isEmpty()) {
            // Already added above
        }

        content.getChildren().add(lblApiInfo);

        return content;
    }

    /**
     * Dodaje red u statistics grid
     */
    private void addStatRow(GridPane grid, int row, String label, String value) {
        Label lblKey = new Label(label);
        lblKey.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        Label lblValue = new Label(value);
        lblValue.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px; -fx-font-weight: bold;");

        grid.add(lblKey, 0, row);
        grid.add(lblValue, 1, row);
    }

    /**
     * HELPER METODE za mapiranje tvojih podataka
     */

    /**
     * Mapira attackTypeId u ljudski čitljiv naziv
     * PRILAGODI OVO prema svojoj bazi podataka!
     */
    private String getAttackTypeName(Long attackTypeId) {
        if (attackTypeId == null) return "Unknown Attack";

        // Ovo prilagodi prema tvojoj tablici attack_types
        switch (attackTypeId.intValue()) {
            case 1:
                return "Port Scan";
            case 2:
                return "Brute Force Attack";
            case 3:
                return "SQL Injection";
            case 4:
                return "DDoS Attack";
            case 5:
                return "Malware Distribution";
            case 6:
                return "Phishing Attempt";
            case 7:
                return "SSH Attack";
            case 8:
                return "Web Application Attack";
            default:
                return "Attack Type #" + attackTypeId;
        }
    }

    /**
     * Mapira severity int u string naziv
     * PRILAGODI OVO prema svojoj logici!
     */
    private String getSeverityName(int severity) {
        // Pretpostavljam: 1=LOW, 2=MEDIUM, 3=HIGH, 4=CRITICAL
        switch (severity) {
            case 1:
                return "LOW";
            case 2:
                return "MEDIUM";
            case 3:
                return "HIGH";
            case 4:
                return "CRITICAL";
            default:
                return "UNKNOWN";
        }
    }

    /**
     * Boja za severity (po int vrijednosti)
     */
    private String getSeverityColor(int severity) {
        switch (severity) {
            case 4:
                return "#ef4444"; // CRITICAL - red
            case 3:
                return "#f97316"; // HIGH - orange
            case 2:
                return "#eab308"; // MEDIUM - yellow
            case 1:
                return "#3b82f6"; // LOW - blue
            default:
                return "#6b7280"; // UNKNOWN - gray
        }
    }

    /**
     * Boja za threat level
     */
    private String getThreatLevelColor(String threatLevel) {
        switch (threatLevel.toUpperCase()) {
            case "CRITICAL":
                return "#dc2626";
            case "HIGH":
                return "#ea580c";
            case "MEDIUM":
                return "#ca8a04";
            case "LOW":
                return "#0284c7";
            case "CLEAN":
                return "#16a34a";
            default:
                return "#6b7280";
        }
    }

    /**
     * Boja za abuse score
     */
    private String getScoreColor(int score) {
        if (score >= 75) return "#ef4444";
        if (score >= 50) return "#f97316";
        if (score >= 25) return "#eab308";
        if (score > 0) return "#3b82f6";
        return "#10b981";
    }

    /**
     * Prikaži prozor i učitaj threat intelligence
     */
    public void show() {
        stage.show();

        // Učitaj threat intelligence u pozadinskoj dretvi
        new Thread(() -> {
            try {
                Thread.sleep(500); // Simulate network delay

                // REST API POZIV - koristi sourceIP iz tvoje klase
                ThreatIntelligenceReport report = abuseIPDBClient.checkIP(incident.getSourceIP());

                // Ažuriraj UI (thread-safe)
                Platform.runLater(() -> {
                    contentContainer.getChildren().clear();
                    contentContainer.getChildren().add(createThreatIntelligenceContent(report));
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    contentContainer.getChildren().clear();

                    Label lblError = new Label("✗ Error loading threat intelligence");
                    lblError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 14px;");

                    Label lblErrorMsg = new Label(e.getMessage());
                    lblErrorMsg.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
                    lblErrorMsg.setWrapText(true);

                    Label lblNote = new Label(
                            "\nNote: You need a valid AbuseIPDB API key.\n" +
                                    "Register for free at: https://www.abuseipdb.com/register"
                    );
                    lblNote.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
                    lblNote.setWrapText(true);

                    VBox errorBox = new VBox(10, lblError, lblErrorMsg, lblNote);
                    errorBox.setAlignment(Pos.CENTER);
                    errorBox.setPadding(new Insets(20));

                    contentContainer.getChildren().add(errorBox);
                });

                System.err.println("✗ Error fetching threat intelligence: " + e.getMessage());
            }
        }).start();
    }
}
