CREATE DATABASE IF NOT EXISTS auth_anomaly_db;
USE auth_anomaly_db;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS login_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    device_info VARCHAR(255),
    is_success BOOLEAN NOT NULL,
    anomaly_score DOUBLE,
    is_anomalous BOOLEAN,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    login_log_id BIGINT,
    alert_type VARCHAR(100) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (login_log_id) REFERENCES login_logs(id) ON DELETE SET NULL
);

-- Insert dummy users
INSERT IGNORE INTO users (username, password_hash, email) VALUES 
('admin', 'dummyhash1', 'admin@example.com'),
('jdoe', 'dummyhash2', 'jdoe@example.com'),
('asmith', 'dummyhash3', 'asmith@example.com');
