package com.security.anomaly.controller;

import com.security.anomaly.model.LoginRequest;
import com.security.anomaly.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/simulate")
    public String simulate() {
        return authService.simulateAttack();
    }
}

@RestController
@CrossOrigin(origins = "*")
class SimulateController {
    @Autowired
    private com.security.anomaly.service.AuthService authService;

    @PostMapping("/api/simulate-attack")
    public String simulateAttack() {
        return authService.simulateAttack();
    }
}
