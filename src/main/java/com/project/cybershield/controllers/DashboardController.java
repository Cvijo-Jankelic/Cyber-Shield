package com.project.cybershield.controllers;


import com.project.cybershield.entities.Incident;
import com.project.cybershield.enums.SpeedLimit;
import com.project.cybershield.enums.SupportedLanguage;
import com.project.cybershield.network.PacketSource;
import com.project.cybershield.test.IdsDebugMain;
import com.project.cybershield.test.PcapLiveSource;
import com.project.cybershield.ui.DownloadProgressPopup;
import com.project.cybershield.ui.IncidentDetailsWindow;
import com.project.cybershield.util.JavaFXProgressListener;
import com.project.cybershield.util.LocaleManager;
import com.project.cybershield.util.ThreatIntelligenceDownloader;
import javafx.animation.AnimationTimer;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.net.URL;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Controller for the CyberShield IDS Dashboard
 * Manages UI updates, data binding, and user interactions
 */
public class DashboardController implements Initializable {
    private Logger logger = LoggerFactory.getLogger(DashboardController.class);

    // Header Controls
    @FXML private Label lblLastUpdate;
    @FXML private Button btnTimeRange;
    @FXML private Button btnExport;
    @FXML private Label lblHeaderTitle;
    @FXML private Label lblStatusText;
    @FXML private Label lblUpdateLabel;

    // Navigation Buttons
    @FXML private Button btnDashboard;
    @FXML private Button btnTrafficLogs;
    @FXML private Button btnRules;
    @FXML private Button btnNetworkMap;
    @FXML private Button btnThreat;
    @FXML private Button btnSettings;

    // Stat Cards Labels
    @FXML private Label lblStatStatus;
    @FXML private Label lblStatRunning;
    @FXML private Label lblStatUptime;
    @FXML private Label lblStatCapture;
    @FXML private Label lblStatCaptureLive;
    @FXML private Label lblStatCaptureMode;
    @FXML private Label lblStatCapturing;
    @FXML private Label lblStatPPS;
    @FXML private Label lblStatAlerts;
    @FXML private Label lblStatAlertsInfo;
    @FXML private Label lblStatCritical;
    @FXML private Label lblStatCriticalBadge;

    // Charts
    @FXML private LineChart<String, Number> alertsLineChart;
    @FXML private PieChart protocolPieChart;
    @FXML private VBox topIPsContainer;
    @FXML private HBox sparklineContainer;

    //Download button
    @FXML private Button btnDownloadThreats;


    // Chart Labels
    @FXML private Label lblChartAlerts;
    @FXML private Label lblChartAlertsSubtitle;
    @FXML private Label lblChartProtocol;
    @FXML private Label lblChartProtocolSubtitle;
    @FXML private Label lblChartIPs;
    @FXML private Label lblChartIPsSubtitle;

    // Table
    @FXML private TableView<Incident> incidentsTable;
    @FXML private TableColumn<Incident, String> colTimestamp;
    @FXML private TableColumn<Incident, String> colRuleId;
    @FXML private TableColumn<Incident, String> colAttackName;
    @FXML private TableColumn<Incident, String> colSeverity;
    @FXML private TableColumn<Incident, String> colSourceDest;
    @FXML private TableColumn<Incident, String> colActions;

    @FXML private Label lblTableTitle;
    @FXML private Label lblTableSubtitle;

    // Search and Pagination
    @FXML private TextField txtSearch;
    @FXML private Button btnFilter;
    @FXML private Label lblTableInfo;
    @FXML private Button btnPrev;
    @FXML private Button btnNext;

    // User Profile
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;

    // Language selector (add to UI)
    @FXML private ComboBox<SupportedLanguage> cmbLanguage;

    // Data
    private ObservableList<Incident> incidentsList;
    private AnimationTimer updateTimer;
    private LocaleManager localeManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== DashboardController.initialize() ===");

        if (incidentsTable == null) {
            System.err.println("❌ incidentsTable is NULL!");
            return;
        }

        System.out.println("✓ incidentsTable connected");
        System.out.println("  Items in table: " + incidentsTable.getItems().size());

        // POSTAVI ROW FACTORY za double-click
        incidentsTable.setRowFactory(tv -> {
            TableRow<Incident> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                // Double-click na neprazan red
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Incident incident = row.getItem();

                    System.out.println("═══════════════════════════════════════════");
                    System.out.println("   DOUBLE-CLICK DETECTED");
                    System.out.println("═══════════════════════════════════════════");
                    System.out.println("  Incident ID: " + incident.getId());
                    System.out.println("  Source IP: " + incident.getSourceIP());
                    System.out.println("  Opening details window...");

                    openIncidentDetails(incident);
                }
            });

            return row;
        });

        System.out.println("✓ Row factory configured - double-click enabled");

        System.out.println("Initializing Dashboard Controller with i18n support...");

        // Initialize locale manager
        localeManager = LocaleManager.getInstance();

        // Initialize data
        initializeData();

        // Setup UI components
        setupLanguageSelector();
        setupCharts();
        setupTable();
        setupSparkline();
        setupIPBars();
        setupEventHandlers();

        // Apply translations
        updateUITexts();

        // Listen for locale changes
        localeManager.currentLocaleProperty().addListener((obs, oldLocale, newLocale) -> {
            updateUITexts();
            updateCharts();
            updateTable();
        });

        // Start real-time updates
        startRealtimeUpdates();

        System.out.println("Dashboard Controller initialized successfully");
    }

    private void openIncidentDetails(Incident incident) {
        try {
            IncidentDetailsWindow window = new IncidentDetailsWindow(
                    incident,
                    "80fa3fc3b826f39ef34dfdab16988ef306da7e879645ae91e3ff39d72ff6836f9d9a5329216e9644"
            );
            window.show();

            System.out.println("✓ Incident Details window opened");

        } catch (Exception e) {
            System.err.println("✗ ERROR opening window: " + e.getMessage());
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to Open Incident Details");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleIncidentDoubleClick(MouseEvent event) {
        if (incidentsTable == null) {
            System.err.println("ERROR: incidentsTable is NULL!");
            System.err.println("Check fx:id in FXML matches field name in Controller");
            return;
        }
        if (event.getClickCount() == 2) {
            Incident selected = incidentsTable.getSelectionModel().getSelectedItem();

            if (selected != null) {
                IncidentDetailsWindow window = new IncidentDetailsWindow(
                        selected,
                        "80fa3fc3b826f39ef34dfdab16988ef306da7e879645ae91e3ff39d72ff6836f9d9a5329216e9644"
                );
                window.show();
            }
        }
        logger.error("Incident double click event triggered", new Throwable("Incident double click event triggered"));
    }

    /**
     * Setup language selector
     */
    @FXML
    private void setupLanguageSelector() {
        if (cmbLanguage != null) {
            cmbLanguage.setItems(FXCollections.observableArrayList(
                    localeManager.getSupportedLanguages()
            ));

            // Custom cell factory to show flag and name
            cmbLanguage.setCellFactory(param -> new ListCell<SupportedLanguage>() {
                @Override
                protected void updateItem(SupportedLanguage lang, boolean empty) {
                    super.updateItem(lang, empty);
                    if (empty || lang == null) {
                        setText(null);
                    } else {
                        setText(lang.getFlag() + " " + lang.getDisplayName());
                    }
                }
            });

            cmbLanguage.setButtonCell(new ListCell<SupportedLanguage>() {
                @Override
                protected void updateItem(SupportedLanguage lang, boolean empty) {
                    super.updateItem(lang, empty);
                    if (empty || lang == null) {
                        setText(null);
                    } else {
                        setText(lang.getFlag() + " " + lang.getDisplayName());
                    }
                }
            });

            // Set current language
            cmbLanguage.setValue(localeManager.getCurrentLanguage());

            // Handle language change
            cmbLanguage.setOnAction(e -> {
                SupportedLanguage selected = cmbLanguage.getValue();
                if (selected != null) {
                    localeManager.setLanguage(selected);
                }
            });
        }
    }

    /**
     * Update all UI texts with current locale
     */
    private void updateUITexts() {
        // Navigation
        if (btnDashboard != null) btnDashboard.setText(localeManager.getString("nav.dashboard"));
        if (btnTrafficLogs != null) btnTrafficLogs.setText(localeManager.getString("nav.trafficLogs"));
        if (btnRules != null) btnRules.setText(localeManager.getString("nav.rulesManagement"));
        if (btnNetworkMap != null) btnNetworkMap.setText(localeManager.getString("nav.networkMap"));
        if (btnThreat != null) btnThreat.setText(localeManager.getString("nav.threatIntelligence"));
        if (btnSettings != null) btnSettings.setText(localeManager.getString("nav.settings"));

        // Header
        if (lblHeaderTitle != null) lblHeaderTitle.setText(localeManager.getString("header.title"));
        if (lblStatusText != null) lblStatusText.setText(localeManager.getString("header.status.online"));
        if (lblUpdateLabel != null) lblUpdateLabel.setText(localeManager.getString("header.lastUpdated"));
        if (btnTimeRange != null) btnTimeRange.setText(localeManager.getString("header.timeRange"));
        if (btnExport != null) btnExport.setText(localeManager.getString("header.export"));

        // User Profile
        if (lblUserName != null) lblUserName.setText(localeManager.getString("user.name"));
        if (lblUserRole != null) lblUserRole.setText(localeManager.getString("user.role"));

        // Stat Cards
        if (lblStatStatus != null) lblStatStatus.setText(localeManager.getString("stat.status"));
        if (lblStatRunning != null) lblStatRunning.setText(localeManager.getString("stat.status.running"));

        if (lblStatCapture != null) lblStatCapture.setText(localeManager.getString("stat.captureMode"));
        if (lblStatCaptureMode != null) lblStatCaptureMode.setText(localeManager.getString("stat.captureMode.promiscuous"));
        if (lblStatCapturing != null) lblStatCapturing.setText(localeManager.getString("stat.captureMode.capturing"));

        if (lblStatPPS != null) lblStatPPS.setText(localeManager.getString("stat.pps"));
        if (lblStatAlerts != null) lblStatAlerts.setText(localeManager.getString("stat.totalAlerts"));
        if (lblStatCritical != null) lblStatCritical.setText(localeManager.getString("stat.criticalAlerts"));
        if (lblStatCriticalBadge != null) lblStatCriticalBadge.setText(localeManager.getString("stat.criticalAlerts.action"));

        // Charts
        if (lblChartAlerts != null) lblChartAlerts.setText(localeManager.getString("chart.alertsOverTime.title"));
        if (lblChartAlertsSubtitle != null) lblChartAlertsSubtitle.setText(localeManager.getString("chart.alertsOverTime.subtitle"));
        if (lblChartProtocol != null) lblChartProtocol.setText(localeManager.getString("chart.protocolDist.title"));
        if (lblChartProtocolSubtitle != null) lblChartProtocolSubtitle.setText(localeManager.getString("chart.protocolDist.subtitle"));
        if (lblChartIPs != null) lblChartIPs.setText(localeManager.getString("chart.topIPs.title"));
        if (lblChartIPsSubtitle != null) lblChartIPsSubtitle.setText(localeManager.getString("chart.topIPs.subtitle"));

        // Table
        if (lblTableTitle != null) lblTableTitle.setText(localeManager.getString("table.title"));
        if (lblTableSubtitle != null) lblTableSubtitle.setText(localeManager.getString("table.subtitle"));
        if (txtSearch != null) txtSearch.setPromptText(localeManager.getString("table.search"));

        // Table columns
        if (colTimestamp != null) colTimestamp.setText(localeManager.getString("column.timestamp"));
        if (colRuleId != null) colRuleId.setText(localeManager.getString("column.ruleId"));
        if (colAttackName != null) colAttackName.setText(localeManager.getString("column.attackName"));
        if (colSeverity != null) colSeverity.setText(localeManager.getString("column.severity"));
        if (colSourceDest != null) colSourceDest.setText(localeManager.getString("column.sourceDest"));
        if (colActions != null) colActions.setText(localeManager.getString("column.actions"));

        // Pagination
        if (btnPrev != null) btnPrev.setText(localeManager.getString("pagination.prev"));
        if (btnNext != null) btnNext.setText(localeManager.getString("pagination.next"));

        // Update table info
        updateTableInfo();
    }

    /**
     * Update table info label
     */
    private void updateTableInfo() {
        if (lblTableInfo != null && incidentsList != null) {
            int showing = Math.min(5, incidentsList.size());
            int total = incidentsList.size();

        }
    }

    /**
     * Populate data from network devices live version
     *
     */

    private void liveNetworkTrafficData() throws PcapNativeException {
        PcapNetworkInterface nif = IdsDebugMain.pickDefaultInterface();
        PacketSource src = new PcapLiveSource(nif);


    }

    /**
     * Initialize sample data
     */
    private void initializeData() {
        incidentsList = FXCollections.observableArrayList(
                new Incident(
                        4002L,
                        LocalDateTime.of(LocalDate.now(), LocalTime.parse("10:42:05")),
                        "192.168.1.50",
                        0,                  // destinationPort (nema u starom zapisu)
                        1L,                 // sqlInjection
                        4,                  // CRITICAL
                        "SQL Injection detected",
                        null
                ),

                new Incident(
                        2021L,
                        LocalDateTime.of(LocalDate.now(), LocalTime.parse("10:41:55")),
                        "45.22.19.11",
                        22,                 // SSH
                        2L,                 // sshBruteForce
                        3,                  // HIGH
                        "SSH brute force attempt",
                        null
                ),

                new Incident(
                        1005L,
                        LocalDateTime.of(LocalDate.now(), LocalTime.parse("10:40:12")),
                        "192.168.1.105",
                        0,
                        3L,                 // portScan
                        2,                  // MEDIUM
                        "Port scan detected on subnet",
                        null
                ),

                new Incident(
                        55L,
                        LocalDateTime.of(LocalDate.now(), LocalTime.parse("10:38:45")),
                        "172.16.0.4",
                        0,
                        4L,                 // icmpFlood
                        1,                  // LOW
                        "ICMP flood activity",
                        null
                ),

                new Incident(
                        1006L,
                        LocalDateTime.of(LocalDate.now(), LocalTime.parse("10:35:10")),
                        "192.168.1.105",
                        0,
                        3L,                 // portScan
                        2,                  // MEDIUM
                        "Repeated port scan attempt",
                        null
                )
        );
    }

    /**
     * Setup charts
     */
    private void setupCharts() {
        setupLineChart();
        setupPieChart();
    }

    /**
     * Setup line chart
     */
    private void setupLineChart() {
        if (alertsLineChart == null) return;

        alertsLineChart.setTitle(null);
        alertsLineChart.setLegendVisible(true);

        updateLineChartData();
    }

    /**
     * Update line chart with localized data
     */
    private void updateLineChartData() {
        if (alertsLineChart == null) return;

        alertsLineChart.getData().clear();

        XYChart.Series<String, Number> lowSeveritySeries = new XYChart.Series<>();
        lowSeveritySeries.setName(localeManager.getString("legend.lowSeverity"));

        XYChart.Series<String, Number> highSeveritySeries = new XYChart.Series<>();
        highSeveritySeries.setName(localeManager.getString("legend.highSeverity"));

        String[] times = {"00:00", "04:00", "08:00", "12:00", "16:00", "20:00", "23:59"};
        int[] lowValues = {5, 10, 8, 15, 12, 20, 25};
        int[] highValues = {1, 1, 2, 5, 3, 8, 6};

        for (int i = 0; i < times.length; i++) {
            lowSeveritySeries.getData().add(new XYChart.Data<>(times[i], lowValues[i]));
            highSeveritySeries.getData().add(new XYChart.Data<>(times[i], highValues[i]));
        }

        alertsLineChart.getData().addAll(lowSeveritySeries, highSeveritySeries);
    }

    /**
     * Setup pie chart
     */
    private void setupPieChart() {
        if (protocolPieChart == null) return;

        updatePieChartData();
        protocolPieChart.setStartAngle(90);
        protocolPieChart.setLabelsVisible(false);
    }

    /**
     * Update pie chart with localized data
     */
    private void updatePieChartData() {
        if (protocolPieChart == null) return;

        protocolPieChart.getData().clear();

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data(localeManager.getString("legend.tcp"), 65),
                new PieChart.Data(localeManager.getString("legend.udp"), 20),
                new PieChart.Data(localeManager.getString("legend.icmp"), 15)
        );

        protocolPieChart.setData(pieChartData);
    }

    /**
     * Update charts when locale changes
     */
    private void updateCharts() {
        updateLineChartData();
        updatePieChartData();
    }

    /**
     * Setup table
     */
    private void setupTable() {
        if (incidentsTable == null) return;

        colTimestamp.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getAttackTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"))));

        colRuleId.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getAttackTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"))));

        colAttackName.setCellValueFactory(data -> {
            String attackKey = "DDos";
            String localizedAttack = localeManager.getString(attackKey);
            return new SimpleStringProperty(localizedAttack);
        });

        colSeverity.setCellValueFactory(data -> {
            String severityKey = "severity." + data.getValue().getSeverity();
            String localizedSeverity = localeManager.getString(severityKey);
            return new SimpleStringProperty(localizedSeverity);
        });

        colSourceDest.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getSourceIP() + " → " +
                        data.getValue().getDestinationPort()));

        colActions.setCellValueFactory(data -> new SimpleStringProperty("⋮"));

        // Custom cell factory for severity badges
        colSeverity.setCellFactory(column -> new TableCell<Incident, String>() {
            @Override
            protected void updateItem(String severity, boolean empty) {
                super.updateItem(severity, empty);

                if (empty || severity == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(severity);
                    badge.getStyleClass().add("severity-badge");

                    String severityLower = severity.toLowerCase();
                    if (severityLower.contains("critical") || severityLower.contains("kritično")) {
                        badge.getStyleClass().add("severity-critical");
                    } else if (severityLower.contains("high") || severityLower.contains("visoko")) {
                        badge.getStyleClass().add("severity-high");
                    } else if (severityLower.contains("medium") || severityLower.contains("srednje")) {
                        badge.getStyleClass().add("severity-medium");
                    } else {
                        badge.getStyleClass().add("severity-low");
                    }

                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        incidentsTable.setItems(incidentsList);
        incidentsTable.setSelectionModel(null);

        updateTableInfo();
    }

    /**
     * Update table when locale changes
     */
    private void updateTable() {
        if (incidentsTable != null) {
            incidentsTable.refresh();
            updateTableInfo();
        }
    }

    /**
     * Setup sparkline
     */
    private void setupSparkline() {
        if (sparklineContainer == null) return;

        sparklineContainer.getChildren().clear();
        sparklineContainer.setAlignment(Pos.BOTTOM_LEFT);
        sparklineContainer.setSpacing(1);

        double[] values = {0.4, 0.6, 0.5, 0.8, 0.7, 0.9, 0.6, 0.8, 0.4, 0.75};

        for (double value : values) {
            Rectangle bar = new Rectangle(2, 40 * value);
            bar.setFill(Color.web("#6366f1"));
            bar.setOpacity(0.5);
            sparklineContainer.getChildren().add(bar);
        }
    }

    /**
     * Setup IP bars
     */
    private void setupIPBars() {
        if (topIPsContainer == null) return;

        topIPsContainer.getChildren().clear();
        topIPsContainer.setSpacing(16);

        String alertsLabel = localeManager.getString("network.alerts");
        addIPBar("192.168.1.105", "1,240 " + alertsLabel, 0.85, "#ef4444");
        addIPBar("10.0.0.55", "850 " + alertsLabel, 0.60, "#f97316");
        addIPBar("172.16.0.4", "420 " + alertsLabel, 0.35, "#3b82f6");
        addIPBar("45.33.22.11", "120 " + alertsLabel, 0.15, "#64748b");
    }

    private void addIPBar(String ip, String count, double percentage, String color) {
        VBox barContainer = new VBox(4);

        HBox labelRow = new HBox();
        labelRow.setAlignment(Pos.CENTER_LEFT);

        Label ipLabel = new Label(ip);
        ipLabel.setStyle("-fx-font-family: 'Courier New', monospace; -fx-text-fill: " + color + "; -fx-font-size: 11px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label countLabel = new Label(count);
        countLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");

        labelRow.getChildren().addAll(ipLabel, spacer, countLabel);

        HBox barBackground = new HBox();
        barBackground.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 4;");
        barBackground.setPrefHeight(8);
        barBackground.setMaxWidth(Double.MAX_VALUE);

        HBox barFill = new HBox();
        barFill.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 4;");
        barFill.setPrefHeight(8);
        barFill.setMaxWidth(percentage * 400);

        barBackground.getChildren().add(barFill);

        barContainer.getChildren().addAll(labelRow, barBackground);
        topIPsContainer.getChildren().add(barContainer);
    }

    /**
     * Setup event handlers
     */
    private void setupEventHandlers() {
        if (btnDashboard != null) btnDashboard.setOnAction(e -> handleDashboardClick());
        if (btnTrafficLogs != null) btnTrafficLogs.setOnAction(e -> handleTrafficLogsClick());
        if (btnRules != null) btnRules.setOnAction(e -> handleRulesClick());
        if (btnNetworkMap != null) btnNetworkMap.setOnAction(e -> handleNetworkMapClick());
        if (btnThreat != null) btnThreat.setOnAction(e -> handleThreatClick());
        if (btnSettings != null) btnSettings.setOnAction(e -> handleSettingsClick());
        if (btnTimeRange != null) btnTimeRange.setOnAction(e -> handleTimeRangeClick());
        if (btnExport != null) btnExport.setOnAction(e -> handleExportClick());
        if (txtSearch != null) txtSearch.textProperty().addListener((obs, oldVal, newVal) -> handleSearch(newVal));
        if (btnPrev != null) btnPrev.setOnAction(e -> handlePreviousPage());
        if (btnNext != null) btnNext.setOnAction(e -> handleNextPage());
    }

    /**
     * Start real-time updates
     */
    private void startRealtimeUpdates() {
        updateTimer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 1_000_000_000) {
                    updateTimestamp();
                    lastUpdate = now;
                }
            }
        };
        updateTimer.start();
    }

    private void updateTimestamp() {
        if (lblLastUpdate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            lblLastUpdate.setText(LocalDateTime.now().format(formatter) + " UTC");
        }
    }

    // Event handlers
    private void handleDashboardClick() {
        System.out.println("Dashboard clicked");
    }

    private void handleTrafficLogsClick() {
        showAlert(localeManager.getString("alert.title.navigation"),
                localeManager.getString("alert.msg.comingSoon"));
    }

    private void handleRulesClick() {
        showAlert(localeManager.getString("alert.title.navigation"),
                localeManager.getString("alert.msg.comingSoon"));
    }

    private void handleNetworkMapClick() {
        showAlert(localeManager.getString("alert.title.navigation"),
                localeManager.getString("alert.msg.comingSoon"));
    }

    private void handleThreatClick() {
        showAlert(localeManager.getString("alert.title.navigation"),
                localeManager.getString("alert.msg.comingSoon"));
    }

    private void handleSettingsClick() {
        showAlert(localeManager.getString("alert.title.navigation"),
                localeManager.getString("alert.msg.comingSoon"));
    }

    private void handleTimeRangeClick() {
        showAlert(localeManager.getString("alert.title.timeRange"),
                localeManager.getString("alert.msg.selectTimeRange"));
    }

    private void handleExportClick() {
        showAlert(localeManager.getString("alert.title.export"),
                localeManager.getString("alert.msg.exporting"));
    }

    private void handleSearch(String query) {
        System.out.println("Search query: " + query);
    }

    private void handlePreviousPage() {
        System.out.println("Previous page clicked");
    }

    private void handleNextPage() {
        System.out.println("Next page clicked");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void cleanup() {
        if (updateTimer != null) {
            updateTimer.stop();
        }
    }
    @FXML
    private void handleDownloadThreatIntelligence() {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("   THREAT INTELLIGENCE DOWNLOAD STARTED   ");
        System.out.println("═══════════════════════════════════════════");

        // 1. KREIRAJ POPUP PROZOR
        DownloadProgressPopup popup = new DownloadProgressPopup();
        popup.setInitialSpeedLimit(SpeedLimit.SLOW);

        // 2. KREIRAJ DOWNLOADER s JavaFX listenerom
        ThreatIntelligenceDownloader downloader;
        try {
            downloader = new ThreatIntelligenceDownloader(
                    Paths.get("threat-intelligence"),
                    new JavaFXProgressListener(popup)
            );
        } catch (Exception e) {
            showError("Cannot initialize downloader", e.getMessage());
            return;
        }

        // 3. POVEŽI SPEED LIMIT SELECTOR
        popup.setSpeedLimitChangeListener(newLimit -> {
            downloader.setSpeedLimit(newLimit);
            System.out.println("[DOWNLOAD] Speed limit changed to: " + newLimit);
        });

        // 4. PRIKAŽI POPUP
        popup.show();

        // 5. POKRENI DOWNLOAD U POZADINSKOJ DRETVI
        new Thread(() -> {
            try {
                popup.setStatus("Connecting to server...");

                // Download Emerging Threats IP blacklist
                String url = "https://rules.emergingthreats.net/blockrules/compromised-ips.txt";
                String filename = "emerging-threats-ips.txt";

                downloader.setSpeedLimit(SpeedLimit.MEDIUM);
                downloader.downloadIpBlackList(url, filename);

                // Čekaj 2 sekunde prije zatvaranja
                Thread.sleep(2000);
                popup.close();

                // Prikaži success poruku u Dashboard-u
                showSuccess("Download Complete",
                        "Threat intelligence data downloaded successfully!");

            } catch (Exception e) {
                // Greška
                popup.setError(e.getMessage());
                System.err.println("[ERROR] Download failed: " + e.getMessage());
                e.printStackTrace();

                // Ne zatvaraj popup odmah - neka user vidi grešku
            }
        }).start();
    }

    /**
     * ALTERNATIVNA METODA - Download više source-ova
     */
    @FXML
    private void handleDownloadMultipleSources() {
        DownloadProgressPopup popup = new DownloadProgressPopup();
        popup.show();

        new Thread(() -> {
            try {
                ThreatIntelligenceDownloader downloader = new ThreatIntelligenceDownloader(
                        Paths.get("threat-intelligence"),
                        new JavaFXProgressListener(popup)
                );

                // Postavi speed limit listener
                popup.setSpeedLimitChangeListener(downloader::setSpeedLimit);

                // Download 1: Emerging Threats
                popup.setStatus("Downloading Emerging Threats blacklist...");
                downloader.downloadIpBlackList(
                        "https://rules.emergingthreats.net/blockrules/compromised-ips.txt",
                        "emerging-threats-ips.txt"
                );

                Thread.sleep(1000);

                // Download 2: Abuse IP DB (ako želite)
                // popup.setStatus("Downloading Abuse IP DB...");
                // downloader.downloadIpBlackList(
                //     "https://raw.githubusercontent.com/stamparm/ipsum/master/ipsum.txt",
                //     "abuse-ips.txt"
                // );

                popup.setComplete("All threat feeds downloaded!");
                Thread.sleep(2000);
                popup.close();

                showSuccess("Success", "All threat intelligence feeds updated!");

            } catch (Exception e) {
                popup.setError(e.getMessage());
            }
        }).start();
    }

    /**
     * Helper - Prikaži error alert
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Helper - Prikaži success alert
     */
    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}