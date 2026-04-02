package com.security.dashboard.model;

public class Alert {
    private Long id;
    private Long userId;
    private Long loginLogId;
    private String alertType;
    private String severity;
    private String createdAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getLoginLogId() { return loginLogId; }
    public void setLoginLogId(Long loginLogId) { this.loginLogId = loginLogId; }
    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
