package no.fint.fintapistatus.controller;

import no.fint.event.model.Event;
import no.fint.event.model.EventUtil;
import no.fint.event.model.health.Health;
import no.fint.event.model.health.HealthStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
public class HealthService {

    private static final String healthCheckURL = "https://play-with-fint.felleskomponent.no/utdanning/timeplan/admin/health";

    @Value("${baseUrl:https://play-with-fint.felleskomponent.no/}")
    private String baseUrl;

    @Autowired
    private WebClient webClient;


    public void healthCheckAll(Map<String, List<String>> domenekart) {
        Set<String> domainKeys = domenekart.keySet();
        for (String mainKey : domainKeys) {
            List<String> secondaryDomains = domenekart.get(mainKey);
            for (String secondaryDomain : secondaryDomains) {
                 healthCheck(mainKey,secondaryDomain);
            }
        }
    }

    public void healthCheck(String hoveddomene, String underdomene) {
        String nyHealthCheckURL = String
                .format("%s%s/%s/admin/health", baseUrl, hoveddomene, underdomene);
        webClient = WebClient.builder()
                .baseUrl(nyHealthCheckURL)
                .defaultHeader("x-client", "testbruker")
                .defaultHeader("x-org-id", "fint.health")
                .build();
        ClientResponse clientResponse = webClient.get().exchange().block();
        try{
            Event healthResult = clientResponse.bodyToMono(Event.class).block();
            addHealthResultToLogg(healthResult);
        }catch (Throwable throwable){
            String key = String.format("%s-%s",hoveddomene,underdomene);
            Event<String> errorEvent = new Event<>();
            errorEvent.addData(throwable.getMessage());
            errorEvent.addData(throwable.getClass().getSimpleName());
            errorEvent.setSource(key);
            addHealthResultToLogg(errorEvent);
        }
    }

    private void addHealthResultToLogg(Event healthResult) {
        if (healthResult != null){
            if (containsHealthyStatus(healthResult)){
                Controller.lastHealthyStatus.put(healthResult.getSource(), healthResult);
            }
            if (Controller.statusLog.get(healthResult.getSource()) != null){
                Controller.statusLog.get(healthResult.getSource()).add(healthResult);
            }else{
                LinkedList<Event> newList = new LinkedList<>();
                newList.add(healthResult);
                Controller.statusLog.put(healthResult.getSource(), newList);
            }
        }
    }
    private boolean containsHealthyStatus(Event event) {
         final String APLICATION_HEALTHY = "APPLICATION_HEALTHY";
        if (event != null && event.getData()!=null){
            List<Health> healthData = EventUtil.convertEventData(event, Health.class);
            Optional<Health> healthyStatus = healthData.stream().filter(health ->
                    HealthStatus.APPLICATION_HEALTHY.name().equals(health.getStatus()))
                    .findAny();
            return healthyStatus.isPresent();
            }
        return false;
    }
}
