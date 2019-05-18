package no.fint.apistatus.controller;

import no.fint.apistatus.exception.HealthCheckNotFound;
import no.fint.apistatus.exception.HealthCheckRequestNotValid;
import no.fint.apistatus.model.HealthCheckRequest;
import no.fint.apistatus.model.HealthCheckRequestValidator;
import no.fint.apistatus.model.HealthCheckResponse;
import no.fint.apistatus.service.HealthRepository;
import no.fint.apistatus.service.HealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api/healthcheck")
class HealthController {

    @Autowired
    private HealthService healthService;

    @Autowired
    private HealthRepository healthRepository;

    @Autowired
    private HealthCheckRequestValidator validator;

    @PostMapping
    public ResponseEntity healthCheck(@RequestBody(required = false) HealthCheckRequest request) {
        if (request == null) {
            healthService.healthCheckAll();
            return ResponseEntity.ok().build();
        }
        if (validator.validate(request)) {
            healthService.healthCheckOne(request.getPath(), request.getEnvironment());
            return ResponseEntity.ok().build();
        }
        throw new HealthCheckRequestNotValid("Request is not valid. Probably because the environment is not valid");
    }


    @GetMapping
    public Map<String, List<HealthCheckResponse>> getHealthChecks() {
        return healthRepository.getHealthChecks();
    }

    @GetMapping("/{environment}")
    public ResponseEntity getHealthCheckByEnvironment(
            @RequestParam(required = false) String path,
            @PathVariable String environment) {

        if (StringUtils.isEmpty(path)) {
            return ResponseEntity.ok(healthRepository.getHealthCheckByEnvironment(environment));
        }

        return ResponseEntity.ok(healthRepository.getHealthCheckByPath(path, environment)
                .orElseThrow(() -> new HealthCheckNotFound(
                        String.format("Health check for path %s in environment %s not found", path, environment))
                )
        );

    }

    @ExceptionHandler(HealthCheckNotFound.class)
    public ResponseEntity handleEntityNotFound(Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e);
    }

    @ExceptionHandler(HealthCheckRequestNotValid.class)
    public ResponseEntity handleHealthCheckRequestNotValid(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
    }
}
