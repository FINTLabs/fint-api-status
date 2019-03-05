package no.fint.fintapistatus.service;

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

    public HealthService() {
        statusLogs = new ConcurrentHashMap<>();
    }

    @Value("${baseUrl:https://play-with-fint.felleskomponent.no/}")
    private String baseUrl;

    @Autowired
    private WebClient webClient;

    @Autowired
    private ComponentService componentService;

    public void healthCheckAll() {
        List<Mono<Event>> listMono = new ArrayList<>();
        componentService.getComponents().forEach(componentConfiguration ->
                listMono.add(healthCheck(componentConfiguration.path)));
        Flux.merge(listMono).collectList().block();
    }

    public Mono<Event> healthCheck(String path) {
        webClient = WebClient.builder()
                .defaultHeader("x-client", "testbruker")
                .defaultHeader("x-org-id", "health.fintlabs.no")
                .build();
        String nyHealthCheckURL = String
                .format("%s%s/admin/health", baseUrl, path);
        return webClient
                .get()
                .uri(nyHealthCheckURL)
                .retrieve()
                .bodyToMono(Event.class)
                .onErrorResume(e -> {
                    String key = path;
                    Event<String> errorEvent = new Event<>();
                    errorEvent.addData(e.getMessage());
                    errorEvent.addData(e.getClass().getSimpleName());
                    errorEvent.setSource(key);
                    errorEvent.setTime(System.currentTimeMillis());
                    return Mono.just(errorEvent);
                })
                .doOnSuccess(this::addHealthResultToLogg);
    }

    private void addHealthResultToLogg(Event healthResult) {
        if (healthResult != null) {
            if (statusLogs.containsKey(healthResult.getSource())) {
                statusLogs.get(healthResult.getSource()).add(healthResult);
            } else {
                statusLogs.put(healthResult.getSource(), new StatusLog(healthResult));
            }
        }
    }

    public boolean containsHealthyStatus(Event event) {
        final String APLICATION_HEALTHY = "APPLICATION_HEALTHY";
        if (event != null && event.getData() != null) {
            List<Health> healthData = EventUtil.convertEventData(event, Health.class);
            Optional<Health> healthyStatus = healthData.stream().filter(health ->
                    HealthStatus.APPLICATION_HEALTHY.name().equals(health.getStatus()))
                    .findAny();
            return healthyStatus.isPresent();
        }
        return false;
    }

    public Map HealthyStatus() {
        ConcurrentHashMap<String, StatusLog> theLog = statusLogs;
        Map<String, Event> map = new HashMap<>();
        if (theLog.size() > 0) {
            theLog.values().forEach(statusLog -> map.put(
                    statusLog.getSource(),
                    statusLog.getLastHealthyStatus(this)));
        }
        return map;
    }

    public Map lastStatus() {
        Map<String, Event> map = new HashMap<>();
        ConcurrentHashMap<String, StatusLog> theLog = HealthService.statusLogs;
        if (theLog.size() > 0) {
            theLog.values().forEach(statusLog -> map.put(
                    statusLog.getSource(),
                    statusLog.getLastStatus()));
        }
        return map;
    }

    public void printExample() {
        if (!statusLogs.isEmpty()) {
            StatusLog statusLog = statusLogs.get("utdanning-vurdering");
            statusLog.skrivUt();
        }
    }
}
