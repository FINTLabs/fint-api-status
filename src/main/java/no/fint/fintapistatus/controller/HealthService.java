package no.fint.fintapistatus.controller;

import no.fint.event.model.Event;
import no.fint.event.model.EventUtil;
import no.fint.event.model.health.Health;
import no.fint.event.model.health.HealthStatus;
import no.fint.fintapistatus.StatusLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class HealthService {
    public static ConcurrentHashMap<String, StatusLog> statusLogs;
    private static final String healthCheckURL = "https://play-with-fint.felleskomponent.no/utdanning/timeplan/admin/health";

    public HealthService() {
        statusLogs = new ConcurrentHashMap<>();
    }

    @Value("${baseUrl:https://play-with-fint.felleskomponent.no/}")
    private String baseUrl;

    @Autowired
    private WebClient webClient;


    public void healthCheckAll(Map<String, List<String>> domenekart) {
        Set<String> domainKeys = domenekart.keySet();
        List<Mono<Event>> liste = new ArrayList<>();
        for (String mainKey : domainKeys) {
            List<String> secondaryDomains = domenekart.get(mainKey);
            for (String secondaryDomain : secondaryDomains) {
                String nyHealthCheckURL = String
                        .format("%s%s/%s/admin/health", baseUrl, mainKey, secondaryDomain);
                webClient = WebClient.builder()
                        .baseUrl(nyHealthCheckURL)
                        .defaultHeader("x-client", "testbruker")
                        .defaultHeader("x-org-id", "fint.health")
                        .build();
                liste.add(webClient.get().retrieve().bodyToMono(Event.class));
            }
            try {
                Flux.merge().subscribe(event -> addHealthResultToLogg((Event)event));
            } catch (Throwable throwable) {
                String key = String.format("%s-%s", "Hvordan hente server???", "Hvordan hente server??");
                Event<String> errorEvent = new Event<>();
                errorEvent.addData(throwable.getMessage());
                errorEvent.addData(throwable.getClass().getSimpleName());
                errorEvent.setSource(key);
                addHealthResultToLogg(errorEvent);
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
            if (statusLogs.containsKey(healthResult.getSource())) {
                statusLogs.get(healthResult.getSource()).add(healthResult);
            }else{
                statusLogs.put(healthResult.getSource(), new StatusLog(healthResult));
            }
        }
    }
    public boolean containsHealthyStatus(Event event) {
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
