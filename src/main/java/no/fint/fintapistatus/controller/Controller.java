/*
The application checks the status of the servers in the application.properties list. The application logs
two different lists. One that collects all the statuses that the servers have had and one that keeps tracks over
the last healthy status check.
 */
package no.fint.fintapistatus.controller;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

@RestController
@EnableScheduling
@RequestMapping(value = "/api")
public class Controller {
    @Autowired
    private HealthService healthService;
    @Value("#{${DOMAINMAP}}")
    private Map<String, List<String>> domainMap;

    @Scheduled(fixedRate = 180000)// Check the health of all the servers
    public void getHealthCheckStatusAll() {
        healthService.healthCheckAll(domainMap);
    }
    /*
    @GetMapping(value = "/healthcheckwithdomene/{domain}/{nextdomain}")//Check health of a specific server.
    public String getHealthCheckStatusByDomene(
            @PathVariable("domain") final String domain,
            @PathVariable("nextdomain") final String nextdomain) {
        healthService.healthCheck(domain, nextdomain);
        return "yes! Ferdig med getHealthCheckStatusByDomene";
    }*/
    @GetMapping(value = "/checkstatus/last_healthy_status")
    public Map checkHealthyStatus(){
        return healthService.HealthyStatus();
    }
    @GetMapping(value = "/checkstatus/last_status")
    public Map checkLastStatus(){
        return healthService.lastStatus();
    }
    @Scheduled(fixedRate = 30000)
    public void printExample(){
        healthService.printExample();
    }
}
