package com.security.anomaly.config;

import com.security.anomaly.model.MLPredictionRequest;
import com.security.anomaly.model.MLPredictionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Random;

@Configuration
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String ML_API_URL = "http://127.0.0.1:8000/predict";

    @Override
    public void run(String... args) throws Exception {
        createTables();
        insertUsers();
        seedLoginEvents();
    }

    private void createTables() {
        // Create users table
        String createUsers = """
                CREATE TABLE IF NOT EXISTS users (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(50) NOT NULL UNIQUE,
                    password_hash VARCHAR(255) NOT NULL,
                    email VARCHAR(100) NOT NULL UNIQUE,
                    created_at TIMESTAMP NOT NULL
                )
                """;
        jdbcTemplate.execute(createUsers);

        // Create login_logs table
        String createLoginLogs = """
                CREATE TABLE IF NOT EXISTS login_logs (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    user_id BIGINT NOT NULL,
                    login_time TIMESTAMP NOT NULL,
                    ip_address VARCHAR(50) NOT NULL,
                    device_info VARCHAR(255) NOT NULL,
                    is_success BOOLEAN NOT NULL,
                    anomaly_score DOUBLE,
                    is_anomalous BOOLEAN NOT NULL
                )
                """;
        jdbcTemplate.execute(createLoginLogs);

        // Create alerts table
        String createAlerts = """
                CREATE TABLE IF NOT EXISTS alerts (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    user_id BIGINT NOT NULL,
                    login_log_id BIGINT NOT NULL,
                    alert_type VARCHAR(50) NOT NULL,
                    severity VARCHAR(20) NOT NULL,
                    created_at TIMESTAMP NOT NULL
                )
                """;
        jdbcTemplate.execute(createAlerts);
    }

    private void insertUsers() {
        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        if (userCount != null && userCount == 0) {
            String insertUser = """
                    INSERT INTO users (username, password_hash, email, created_at)
                    VALUES (?, ?, ?, ?)
                    """;
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            jdbcTemplate.update(insertUser, "user1", "hashed_pwd1", "user1@example.com", now);
            jdbcTemplate.update(insertUser, "admin", "hashed_pwd_admin", "admin@example.com", now);
        }
    }

    private void seedLoginEvents() {
        Integer logCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM login_logs", Integer.class);
        if (logCount != null && logCount == 0) {
            System.out.println("Seeding 20 login events...");
            Random random = new Random();

            String[] devices = {"Mozilla/5.0 Windows NT 10.0", "Mac OS X 10_15_7", "iPhone CPU OS 16_0"};
            String[] commonIps = {"192.168.1.10", "192.168.1.55"};
            String[] unusualIps = {"203.0.113.5", "198.51.100.22"};
            
            for (int i = 0; i < 20; i++) {
                // Simulate chronological spread: between 48 hours ago and now
                int hoursAgo = 48 - (i * 2);
                LocalDateTime eventTime = LocalDateTime.now().minusHours(hoursAgo);
                Timestamp timestamp = Timestamp.valueOf(eventTime);
                
                Long userId = (random.nextBoolean()) ? 1L : 2L;
                int loginHour = eventTime.getHour();
                
                // Introduce anomalies randomly
                boolean simulateAnomaly = (random.nextInt(10) > 7); // ~20% chance of anomaly
                
                int failedAttempts = simulateAnomaly ? (3 + random.nextInt(5)) : (random.nextInt(2));
                int isNewDevice = simulateAnomaly ? 1 : 0;
                int isNewIp = simulateAnomaly ? 1 : 0;
                
                String device = simulateAnomaly ? "Unknown UnknownBrowser" : devices[random.nextInt(devices.length)];
                String ip = simulateAnomaly ? unusualIps[random.nextInt(unusualIps.length)] : commonIps[random.nextInt(commonIps.length)];
                
                // Call ML API
                MLPredictionRequest mlReq = new MLPredictionRequest();
                mlReq.setUserId(userId);
                mlReq.setLoginHour(loginHour);
                mlReq.setFailedAttempts(failedAttempts);
                mlReq.setIsNewDevice(isNewDevice);
                mlReq.setIsNewIp(isNewIp);

                Double anomalyScore = 0.0;
                boolean isAnomalous = false;

                try {
                    ResponseEntity<MLPredictionResponse> response = restTemplate.postForEntity(ML_API_URL, mlReq, MLPredictionResponse.class);
                    if (response.getBody() != null) {
                        anomalyScore = response.getBody().getAnomalyScore();
                        isAnomalous = response.getBody().getIsAnomalous() == 1;
                    }
                } catch (Exception e) {
                    System.err.println("Warning: Could not reach ML API during seeding for event " + i + ". Error: " + e.getMessage());
                    // Fallback to simplistic rule if ML is down
                    isAnomalous = simulateAnomaly;
                    anomalyScore = simulateAnomaly ? 1.8 : -0.5;
                }

                // Insert Log
                String insertLog = """
                        INSERT INTO login_logs (user_id, login_time, ip_address, device_info, is_success, anomaly_score, is_anomalous) 
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        """;
                jdbcTemplate.update(insertLog, userId, timestamp, ip, device, true, anomalyScore, isAnomalous);
                
                // Create Alert if Anomalous
                if (isAnomalous) {
                    Long logId = jdbcTemplate.queryForObject(
                        "SELECT id FROM login_logs ORDER BY id DESC LIMIT 1", Long.class);
                        
                    if (logId != null) {
                        String insertAlert = """
                                INSERT INTO alerts (user_id, login_log_id, alert_type, severity, created_at) 
                                VALUES (?, ?, ?, ?, ?)
                                """;
                        String severity = anomalyScore > 1.5 ? "HIGH" : "MEDIUM";
                        jdbcTemplate.update(insertAlert, userId, logId, "Anomalous Login Detected", severity, timestamp);
                    }
                }
            }
            
            System.out.println("20 Seed events injected successfully.");
        }
    }
}
