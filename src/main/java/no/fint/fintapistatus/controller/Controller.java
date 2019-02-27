/*
The application checks the status of the servers in the application.properties list. The application logs
two different lists. One that collects all the statuses that the servers have had and one that keeps tracks over
the last healthy status check.
 */
package no.fint.fintapistatus.controller;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import no.fint.event.model.Event;
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
    //Finnes det noen ConcurrentList? Eller er det greit siden add er O(1)?
    static ConcurrentHashMap<String,LinkedList<Event>> statusLog = new ConcurrentHashMap<>();//Collects all check status
    static ConcurrentHashMap<String,Event> lastHealthyStatus = new ConcurrentHashMap<>();//Collects the last healthy status

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
    @GetMapping(value = "/checkstatus/{typeOfLog}")
    public String checkStatus(@PathVariable final String typeOfLog){
        StringBuilder completeStatus = new StringBuilder();
        Map map = null;
        if ((typeOfLog.equals("healthy") || (typeOfLog.equals("lastlog")))){
            completeStatus.append(
                    String.format("<br><b>%s</b><br><br>", typeOfLog));
            map = ((typeOfLog.equals("healthy"))) ? statusLog : lastHealthyStatus;
        }
        if (map!=null){
            Set<String> mainDomains = map.keySet();
            for (String mainDomain : mainDomains){
                Event lastLog = typeOfLog.equals("healthy") ? statusLog.get(mainDomain).getLast()
                        : lastHealthyStatus.get(mainDomain);
                completeStatus.append(String.format("<br><br>%s: <br>%s <br>%s"
                        ,lastLog.getSource()
                        ,lastLog.getData().toString()
                        ,lastLog.getMessage()));
            }
        }else {return "Noe har gått galt!";}
        return completeStatus.toString();
    }
}
