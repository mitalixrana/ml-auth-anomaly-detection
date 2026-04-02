package com.security.anomaly.model;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class MLPredictionRequest {
    @JsonProperty("user_id")
    private Long userId;
    
    @JsonProperty("login_hour")
    private Integer loginHour;
    
    @JsonProperty("failed_attempts")
    private Integer failedAttempts;
    
    @JsonProperty("is_new_device")
    private Integer isNewDevice;
    
    @JsonProperty("is_new_ip")
    private Integer isNewIp;
}
