package no.fint.apistatus.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.apistatus.model.HealthCheckResponse;
import no.fint.event.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static no.fint.apistatus.ApplicationConfig.TargetService;
import static no.fint.apistatus.ApplicationConfig.TargetService.ServiceTypes.HEALTH;

@Slf4j
@Service
public class HealthService {
    private final ConcurrentHashMap<String, Event> completedHealthChecks = new ConcurrentHashMap<>();

    @Autowired
    @TargetService(HEALTH)
    private WebClient webClient;

    @Autowired
    private ComponentService componentService;

    @Scheduled(fixedRateString = "${fint.apistatus.healthcheck-rate-ms:180000}", initialDelay = 10000)
    public void healthCheckAll() {
        log.info("Running health chekcs...");
        List<Mono<Event>> events = componentService.getComponents().stream()
                .map(componentConfiguration -> healthCheck(componentConfiguration.getPath())).collect(Collectors.toList());
        Flux.merge(events).collectList().block();
        log.info("End running health chekcs");
    }

    private Mono<Event> healthCheck(String path) {
        String newHealthCheckURL = String.format("%s/admin/health", path);
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
                .doOnSuccess(healthResult -> completedHealthChecks.put(path, healthResult));
    }

    public HealthCheckResponse getHealthCheck(String path) {
        Event event = completedHealthChecks.get(path);
        return new HealthCheckResponse(path, event);
    }

    public List<HealthCheckResponse> getHealthChecks() {
        return completedHealthChecks.entrySet().stream()
                .map(entry -> new HealthCheckResponse(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }

    public void healthCheckOne(String path) {
        try {
            healthCheck(path).block();
        } catch (Throwable t) {
            log.error("Exception occurred during single health check", t);
            throw t;
        }
    }
}
