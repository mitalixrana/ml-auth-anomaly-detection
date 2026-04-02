package com.security.anomaly.model;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String ipAddress;
    private String deviceInfo;
}
