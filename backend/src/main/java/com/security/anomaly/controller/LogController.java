package com.security.anomaly.controller;

import com.security.anomaly.model.LoginLog;
import com.security.anomaly.repository.LoginLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
@CrossOrigin(origins = "*")
public class LogController {

    @Autowired
    private LoginLogRepository loginLogRepository;

    @GetMapping
    public List<LoginLog> getLogs() {
        return loginLogRepository.findAll();
    }
}
