package com.security.anomaly.model;

import lombok.Data;
import java.sql.Timestamp;

@Data
public class Alert {
    private Long id;
    private Long userId;
    private Long loginLogId;
    private String alertType;
    private String severity; // e.g., MEDIUM, HIGH
    private Timestamp createdAt;
}
