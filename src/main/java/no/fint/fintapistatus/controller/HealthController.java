/*
The application checks the status of the servers in the application.properties list. The application logs
two different lists. One that collects all the statuses that the servers have had and one that keeps tracks over
the last healthy status check.
 */
package no.fint.fintapistatus.controller;

import no.fint.fintapistatus.service.HealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@EnableScheduling
@RequestMapping(value = "/api")
class HealthController {

    @Autowired
    private HealthService healthService;

     // Here you can check a specific servers path by adding it to the Post data path variable.
    @PostMapping(value = "/healthcheck")//Check health of a specific server.
    private ResponseEntity healthCheckByDomain(@RequestParam("path") String path) {
        healthService.healthCheckOne(path);
        return ResponseEntity.ok(HttpStatus.OK);
    } //TEST:: curl -d "path=/administrasjon/personal" -X POST localhost:8080/api/healthcheck

    @GetMapping(value = "/health")
    private Map latestStatus() {
        return healthService.getStatus();
    }
}
