package com.security.anomaly.model;

import lombok.Data;

@Data
public class MLPredictionResponse {
    private Double anomalyScore;
    private Integer isAnomalous; // 1 for anomalous, 0 for normal
}
