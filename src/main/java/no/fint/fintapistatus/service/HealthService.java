package no.fint.fintapistatus.service;

import no.fint.event.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@EnableScheduling
public class HealthService {
    public static ConcurrentHashMap<String, Event> completeStatusMap;

    @Value("${baseUrl:https://play-with-fint.felleskomponent.no/}")
    private String baseUrl;

    @Autowired
    private WebClient webClient;

    @Autowired
    private ComponentService componentService;

    public HealthService() {
        completeStatusMap = new ConcurrentHashMap<>();
    }

    /*
    Check application.properties for fixedRateString.
     */
    @Scheduled(fixedRateString = "${servercheck.time}", initialDelay = 1000)
    public void healthCheckAll() {
        List<Mono<Event>> listMono = new ArrayList<>();
        componentService.getComponents()
                .forEach(componentConfiguration ->
                        listMono.add(healthCheck(componentConfiguration.path)));
        Flux.merge(listMono).collectList().block();
    }

    //Makes and returns a healthcheck based on parameter path. Returns errorEvent if Event creation fails.
    public Mono<Event> healthCheck(String path) {
        webClient = WebClient.builder()
                .defaultHeader("x-client", "testbruker")
                .defaultHeader("x-org-id", "health.fintlabs.no")
                .build();
        String newHealthCheckURL = String
                .format("%s%s/admin/health", baseUrl, path);
        return webClient
                .get()
                .uri(newHealthCheckURL)
                .retrieve()
                .bodyToMono(Event.class)
                .onErrorResume(e -> {
                    Event<String> errorEvent = new Event<>();
                    errorEvent.addData(e.getMessage());
                    errorEvent.addData(e.getClass().getSimpleName());
                    errorEvent.setSource(path);
                    errorEvent.setTime(System.currentTimeMillis());
                    return Mono.just(errorEvent);
                })
                .doOnSuccess(healthResult -> completeStatusMap.put(path, healthResult));
    }

    public Map getStatus() {
        return completeStatusMap;
    }
}
