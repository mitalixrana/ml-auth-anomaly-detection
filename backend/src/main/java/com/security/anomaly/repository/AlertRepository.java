package com.security.anomaly.repository;

import com.security.anomaly.model.Alert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class AlertRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Alert> alertRowMapper = (rs, rowNum) -> {
        Alert alert = new Alert();
        alert.setId(rs.getLong("id"));
        alert.setUserId(rs.getLong("user_id"));
        alert.setLoginLogId(rs.getLong("login_log_id"));
        alert.setAlertType(rs.getString("alert_type"));
        alert.setSeverity(rs.getString("severity"));
        alert.setCreatedAt(rs.getTimestamp("created_at"));
        return alert;
    };

    public void save(Alert alert) {
        String sql = "INSERT INTO alerts (user_id, login_log_id, alert_type, severity) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, alert.getUserId(), alert.getLoginLogId(), alert.getAlertType(), alert.getSeverity());
    }

    public List<Alert> findAll() {
        String sql = "SELECT * FROM alerts ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, alertRowMapper);
    }
}
