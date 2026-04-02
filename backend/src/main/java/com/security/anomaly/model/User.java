package com.security.anomaly.model;

import lombok.Data;
import java.sql.Timestamp;

@Data
public class User {
    private Long id;
    private String username;
    private String passwordHash;
    private String email;
    private Timestamp createdAt;
}
