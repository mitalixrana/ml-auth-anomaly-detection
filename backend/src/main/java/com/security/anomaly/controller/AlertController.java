package com.security.anomaly.controller;

import com.security.anomaly.model.Alert;
import com.security.anomaly.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "*")
public class AlertController {

    @Autowired
    private AlertRepository alertRepository;

    @GetMapping
    public List<Alert> getAlerts() {
        return alertRepository.findAll();
    }
}
