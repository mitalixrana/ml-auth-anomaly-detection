package com.security.anomaly.repository;

import com.security.anomaly.model.LoginLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class LoginLogRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<LoginLog> logRowMapper = (rs, rowNum) -> {
        LoginLog log = new LoginLog();
        log.setId(rs.getLong("id"));
        log.setUserId(rs.getLong("user_id"));
        log.setLoginTime(rs.getTimestamp("login_time"));
        log.setIpAddress(rs.getString("ip_address"));
        log.setDeviceInfo(rs.getString("device_info"));
        log.setSuccess(rs.getBoolean("is_success"));
        log.setAnomalyScore(rs.getDouble("anomaly_score"));
        log.setAnomalous(rs.getBoolean("is_anomalous"));
        return log;
    };

    public void save(LoginLog log) {
        String sql = "INSERT INTO login_logs (user_id, ip_address, device_info, is_success, anomaly_score, is_anomalous) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, log.getUserId(), log.getIpAddress(), log.getDeviceInfo(),
                log.isSuccess(), log.getAnomalyScore(), log.getAnomalous());
    }

    public int countFailedAttempts(Long userId, int hours) {
        String sql = "SELECT COUNT(*) FROM login_logs WHERE user_id = ? AND is_success = false AND login_time >= NOW() - INTERVAL ? HOUR";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, hours);
        return count != null ? count : 0;
    }

    public boolean isNewDevice(Long userId, String deviceInfo) {
        String sql = "SELECT COUNT(*) FROM login_logs WHERE user_id = ? AND device_info = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, deviceInfo);
        return count == null || count == 0;
    }

    public boolean isNewIp(Long userId, String ipAddress) {
        String sql = "SELECT COUNT(*) FROM login_logs WHERE user_id = ? AND ip_address = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, ipAddress);
        return count == null || count == 0;
    }

    public List<LoginLog> findAll() {
        String sql = "SELECT * FROM login_logs ORDER BY login_time DESC";
        return jdbcTemplate.query(sql, logRowMapper);
    }
}
