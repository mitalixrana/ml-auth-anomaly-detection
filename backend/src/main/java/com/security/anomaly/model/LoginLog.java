package com.security.anomaly.model;

import lombok.Data;
import java.sql.Timestamp;

@Data
public class LoginLog {
    private Long id;
    private Long userId;
    private Timestamp loginTime;
    private String ipAddress;
    private String deviceInfo;
    private boolean success;
    private Double anomalyScore;
    private Boolean anomalous;
}
