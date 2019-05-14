package no.fint.apistatus.controller;

import com.google.common.collect.Lists;
import no.fint.apistatus.model.HealthCheckRequest;
import no.fint.apistatus.model.HealthCheckResponse;
import no.fint.apistatus.service.HealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/healthcheck")
class HealthController {

    @Autowired
    private HealthService healthService;

    @PostMapping
    public void healthCheck(@RequestBody HealthCheckRequest healthCheckRequest) {
        healthService.healthCheckOne(healthCheckRequest.getApiBaseUrl());
    }

    @GetMapping
    public List<HealthCheckResponse> getHealthChecks(@RequestParam(required = false) String path) {
        if (path == null) {
            return healthService.getHealthChecks();
        } else {
            return Lists.newArrayList(healthService.getHealthCheck(path));
        }
    }
}
