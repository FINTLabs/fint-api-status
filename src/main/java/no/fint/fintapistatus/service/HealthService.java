package no.fint.fintapistatus.service;

import no.fint.event.model.Event;
import no.fint.event.model.EventUtil;
import no.fint.event.model.health.Health;
import no.fint.event.model.health.HealthStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@EnableScheduling
public class HealthService {
    public static ConcurrentHashMap<String, Event> statusMap;

    @Value("${baseUrl:https://play-with-fint.felleskomponent.no/}")
    private String baseUrl;

    @Autowired
    private WebClient webClient;

    @Autowired
    private ComponentService componentService;

    public HealthService() {
        statusMap = new ConcurrentHashMap<>();
    }

    @Scheduled(fixedRateString ="${servercheck.time}", initialDelay=1000)
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
                    Event<String> errorEvent = new Event<>();
                    errorEvent.addData(e.getMessage());
                    errorEvent.addData(e.getClass().getSimpleName());
                    errorEvent.setSource(path);
                    errorEvent.setTime(System.currentTimeMillis());
                    return Mono.just(errorEvent);
                })
                .doOnSuccess(healthResult -> statusMap.put(path,healthResult));
    }

    public Map lastStatus() {
        return statusMap;
    }
}
