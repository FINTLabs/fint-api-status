package no.fint.apistatus.controller;

import no.fint.apistatus.model.HealthCheckRequest;
import no.fint.apistatus.model.HealthCheckResponse;
import no.fint.apistatus.service.HealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/healthcheck")
class HealthController {

    @Autowired
    private HealthService healthService;

    @PostMapping
    private void healthCheck(@RequestBody HealthCheckRequest healthCheckRequest) {
        healthService.healthCheckOne(healthCheckRequest.getApiBaseUrl());
    }

    @GetMapping
    private HealthCheckResponse getLatestHealthCheck() {
        return healthService.getStatus();
    }
}
