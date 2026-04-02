package com.security.dashboard.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.security.dashboard.model.Alert;
import com.security.dashboard.model.LoginLog;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DashboardController {

    // Metrics
    @FXML private Label lblTotalLogins;
    @FXML private Label lblSuccess;
    @FXML private Label lblFailed;
    @FXML private Label lblAnomalies;
    @FXML private Label lblSuspiciousIps;

    // Table: Logs
    @FXML private TableView<LoginLog> logsTable;
    @FXML private TableColumn<LoginLog, Long> colLogUser;
    @FXML private TableColumn<LoginLog, String> colLogTime;
    @FXML private TableColumn<LoginLog, String> colLogIp;
    @FXML private TableColumn<LoginLog, String> colLogDevice;
    @FXML private TableColumn<LoginLog, Boolean> colLogSuccess;
    @FXML private TableColumn<LoginLog, Double> colLogScore;

    // Table: Alerts
    @FXML private TableView<Alert> alertsTable;
    @FXML private TableColumn<Alert, Long> colAlertUser;
    @FXML private TableColumn<Alert, String> colAlertType;
    @FXML private TableColumn<Alert, String> colAlertSeverity;
    @FXML private TableColumn<Alert, String> colAlertTime;

    // Table: Risky IPs
    @FXML private TableView<RiskyIp> riskyIpTable;
    @FXML private TableColumn<RiskyIp, String> colRiskyIp;
    @FXML private TableColumn<RiskyIp, Double> colRiskyScore;

    // Charts
    @FXML private LineChart<String, Number> timelineChart;
    @FXML private PieChart statusPieChart;
    @FXML private BarChart<String, Number> deviceChart;
    @FXML private BarChart<String, Number> ipChart;

    // Filters
    @FXML private TextField filterUserId;
    @FXML private TextField filterIp;
    @FXML private ComboBox<String> filterStatus;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private ScheduledExecutorService scheduler;

    private ObservableList<LoginLog> masterLogData = FXCollections.observableArrayList();
    private ObservableList<Alert> masterAlertData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        initTables();
        initFilters();

        fetchData();

        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::fetchData, 5, 5, TimeUnit.SECONDS);
    }

    private void initTables() {
        colLogUser.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colLogTime.setCellValueFactory(new PropertyValueFactory<>("loginTime"));
        colLogIp.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));
        colLogDevice.setCellValueFactory(new PropertyValueFactory<>("deviceInfo"));
        colLogSuccess.setCellValueFactory(new PropertyValueFactory<>("success"));
        colLogScore.setCellValueFactory(new PropertyValueFactory<>("anomalyScore"));

        colAlertUser.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colAlertType.setCellValueFactory(new PropertyValueFactory<>("alertType"));
        colAlertSeverity.setCellValueFactory(new PropertyValueFactory<>("severity"));
        colAlertTime.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        colRiskyIp.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));
        colRiskyScore.setCellValueFactory(new PropertyValueFactory<>("maxScore"));

        // Row factory for colors
        logsTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(LoginLog log, boolean empty) {
                super.updateItem(log, empty);
                getStyleClass().removeAll("row-anomalous", "row-suspicious", "row-normal");
                
                if (log == null || empty) {
                    setStyle("");
                } else if (log.isAnomalous()) {
                    getStyleClass().add("row-anomalous");
                } else if (log.getAnomalyScore() != null && log.getAnomalyScore() > 1.0) {
                    getStyleClass().add("row-suspicious");
                } else {
                    getStyleClass().add("row-normal");
                }
            }
        });
    }

    private void initFilters() {
        filterStatus.setItems(FXCollections.observableArrayList("All", "Normal", "Anomalous"));
        filterStatus.setValue("All");

        filterUserId.textProperty().addListener((obs, oldV, newV) -> applyFilters());
        filterIp.textProperty().addListener((obs, oldV, newV) -> applyFilters());
        filterStatus.valueProperty().addListener((obs, oldV, newV) -> applyFilters());
    }

    private void applyFilters() {
        FilteredList<LoginLog> filteredData = new FilteredList<>(masterLogData, log -> {
            boolean matchesUser = filterUserId.getText().isEmpty() || 
                String.valueOf(log.getUserId()).contains(filterUserId.getText());
            boolean matchesIp = filterIp.getText().isEmpty() || 
                (log.getIpAddress() != null && log.getIpAddress().contains(filterIp.getText()));
            
            boolean matchesStatus = true;
            String status = filterStatus.getValue();
            if ("Normal".equals(status)) {
                matchesStatus = !log.isAnomalous();
            } else if ("Anomalous".equals(status)) {
                matchesStatus = log.isAnomalous();
            }
            
            return matchesUser && matchesIp && matchesStatus;
        });

        logsTable.setItems(filteredData);
    }

    @FXML
    public void fetchData() {
        fetchLogs();
        fetchAlerts();
    }

    @FXML
    public void simulateAttack() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/simulate-attack"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    System.out.println("Attack simulated: " + response.body());
                    fetchData();
                })
                .exceptionally(e -> {
                    System.err.println("Attack simulation failed: " + e.getMessage());
                    return null;
                });
    }

    private void fetchLogs() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/logs"))
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(json -> {
                    try {
                        List<LoginLog> logs = gson.fromJson(json, new TypeToken<List<LoginLog>>(){}.getType());
                        if (logs != null) {
                            Platform.runLater(() -> {
                                masterLogData.setAll(logs);
                                applyFilters();
                                updateMetrics(logs);
                                updateCharts(logs);
                                updateRiskyIps(logs);
                            });
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing logs: " + e.getMessage());
                    }
                });
    }

    private void fetchAlerts() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/alerts"))
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(json -> {
                    try {
                        List<Alert> alerts = gson.fromJson(json, new TypeToken<List<Alert>>(){}.getType());
                        if (alerts != null) {
                            Platform.runLater(() -> {
                                masterAlertData.setAll(alerts);
                                alertsTable.setItems(masterAlertData);
                            });
                        }
                    } catch (Exception e) {
                        // ignore if JSON is bad, just skip
                    }
                });
    }

    private void updateMetrics(List<LoginLog> logs) {
        long total = logs.size();
        long success = logs.stream().filter(LoginLog::isSuccess).count();
        long anomalies = logs.stream().filter(LoginLog::isAnomalous).count();
        long suspiciousIps = logs.stream()
            .filter(l -> l.getAnomalyScore() != null && l.getAnomalyScore() > 1.2)
            .map(LoginLog::getIpAddress)
            .distinct().count();

        lblTotalLogins.setText(String.valueOf(total));
        lblSuccess.setText(String.valueOf(success));
        lblFailed.setText(String.valueOf(total - success));
        lblAnomalies.setText(String.valueOf(anomalies));
        lblSuspiciousIps.setText(String.valueOf(suspiciousIps));
    }

    private void updateCharts(List<LoginLog> logs) {
        // Pie Chart
        long normalCount = logs.stream().filter(l -> !l.isAnomalous()).count();
        long anomalousCount = logs.size() - normalCount;
        
        statusPieChart.getData().clear();
        statusPieChart.getData().add(new PieChart.Data("Normal (" + normalCount + ")", normalCount));
        statusPieChart.getData().add(new PieChart.Data("Anomalous (" + anomalousCount + ")", anomalousCount));

        // Device Bar Chart
        Map<String, Long> deviceCounts = logs.stream()
             .collect(Collectors.groupingBy(LoginLog::getDeviceInfo, Collectors.counting()));
             
        XYChart.Series<String, Number> devSeries = new XYChart.Series<>();
        deviceCounts.forEach((dev, count) -> devSeries.getData().add(new XYChart.Data<>(dev, count)));
        deviceChart.getData().clear();
        deviceChart.getData().add(devSeries);
        
        // IP Bar Chart
        Map<String, Long> ipCounts = logs.stream()
            .collect(Collectors.groupingBy(LoginLog::getIpAddress, Collectors.counting()));
            
        XYChart.Series<String, Number> ipSeries = new XYChart.Series<>();
        ipCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .forEach(e -> ipSeries.getData().add(new XYChart.Data<>(e.getKey(), e.getValue())));
        
        ipChart.getData().clear();
        ipChart.getData().add(ipSeries);
        
        // Timeline Chart (Simple group by hour representation)
        // Group by hour substring of loginTime if formatting is known
        XYChart.Series<String, Number> timeSeries = new XYChart.Series<>();
        timeSeries.setName("Logins");
        Map<String, Long> timeGroup = logs.stream()
            .collect(Collectors.groupingBy(
                l -> l.getLoginTime() != null && l.getLoginTime().length() > 13 ? l.getLoginTime().substring(11, 13) + ":00" : "Unknown",
                Collectors.counting()
            ));
        
        timeGroup.entrySet().stream().sorted(Map.Entry.comparingByKey())
                 .forEach(e -> timeSeries.getData().add(new XYChart.Data<>(e.getKey(), e.getValue())));
        timelineChart.getData().clear();
        timelineChart.getData().add(timeSeries);
    }

    private void updateRiskyIps(List<LoginLog> logs) {
        Map<String, Double> ipRank = new HashMap<>();
        for (LoginLog log : logs) {
            if (log.getIpAddress() == null || log.getAnomalyScore() == null) continue;
            // keep highest anomaly score per IP
            ipRank.put(log.getIpAddress(), Math.max(ipRank.getOrDefault(log.getIpAddress(), 0.0), log.getAnomalyScore()));
        }
        
        List<RiskyIp> risky = ipRank.entrySet().stream()
            .filter(e -> e.getValue() > 1.0)
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .map(e -> new RiskyIp(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
            
        riskyIpTable.setItems(FXCollections.observableArrayList(risky));
    }

    public void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    public static class RiskyIp {
        private String ipAddress;
        private Double maxScore;
        
        public RiskyIp(String ipAddress, Double maxScore) {
            this.ipAddress = ipAddress;
            this.maxScore = maxScore;
        }
        public String getIpAddress() { return ipAddress; }
        public Double getMaxScore() { return Math.round(maxScore * 100.0) / 100.0; }
    }
}
