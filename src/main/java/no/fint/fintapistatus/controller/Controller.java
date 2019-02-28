/*
The application checks the status of the servers in the application.properties list. The application logs
two different lists. One that collects all the statuses that the servers have had and one that keeps tracks over
the last healthy status check.
 */
package no.fint.fintapistatus.controller;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import no.fint.event.model.Event;
import no.fint.event.model.health.HealthStatus;
import no.fint.fintapistatus.StatusLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api")
public class Controller {
    @Autowired
    private HealthService healthService;
    @Autowired
    private Environment env;

    @GetMapping(value = "/healthcheck/all") // Check the health of all the servers
    public String getHealthCheckStatusAll() {
        TreeMap<String, List<String>> domainMap = new TreeMap<>();
        List<String> domene1Underdomener =
                Arrays.asList(env.getProperty("DOMAIN1ENTRIES").split(","));
        List<String> domene2Underdomener =
                Arrays.asList(env.getProperty("DOMAIN2ENTRIES").split(","));
        domainMap.put(env.getProperty("TOP_DOMAIN1"), domene1Underdomener);
        domainMap.put(env.getProperty("TOP_DOMAIN2"), domene2Underdomener);
        healthService.healthCheckAll(domainMap);
        return "suksess!";
    }
    @GetMapping(value = "/healthcheckwithdomene/{domain}/{nextdomain}")//Check health of a specific server.
    public String getHealthCheckStatusByDomene(
            @PathVariable("domain") final String domain,
            @PathVariable("nextdomain") final String nextdomain) {
        healthService.healthCheck(domain, nextdomain);
        return "yes! Ferdig med getHealthCheckStatusByDomene";
    }
    @GetMapping(value = "/checkstatus/last_healthy_status")
    public String checkHealthyStatus(){
        ConcurrentHashMap<String, StatusLog> theLog = HealthService.statusLogs;
        StringBuilder completeStatus = new StringBuilder();
        if (theLog.size() > 0) {
            theLog.values().forEach(
                    statusLog -> completeStatus.append(
                            String.format("<br><br>%s",
                                    statusLog.getLastHealthyStatus(healthService))));
        }
        return completeStatus.toString();
    }
    @GetMapping(value = "/checkstatus/last_status")
    public String checkLastStatus(){
        ConcurrentHashMap<String, StatusLog> theLog = HealthService.statusLogs;
        StringBuilder completeStatus = new StringBuilder();
        if (theLog.size() > 0) {
            theLog.values().forEach(
                    statusLog -> completeStatus.append(String.format("<br><br>%s", statusLog.getLastStatus())));
        }
        return completeStatus.toString();
    }
}
