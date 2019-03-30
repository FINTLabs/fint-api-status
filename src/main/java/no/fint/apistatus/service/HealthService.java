package no.fint.apistatus.service;

import no.fint.apistatus.model.HealthCheckResponse;
import no.fint.event.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
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

@Service
@EnableScheduling
public class HealthService {
    private ConcurrentHashMap<String, Event> completeStatusMap = new ConcurrentHashMap<>();

    @Autowired
    @TargetService(HEALTH)
    private WebClient webClient;

    @Autowired
    private ComponentService componentService;

    @Scheduled(fixedRateString = "${servercheck.time}", initialDelay = 10000)
    public void healthCheckAll() {
        List<Mono<Event>> listMono = componentService.getComponents()
                .stream().map(componentConfiguration ->
                        healthCheck(componentConfiguration.getPath())).collect(Collectors.toList());
        Flux.merge(listMono).collectList().block();
    }

    private Mono<Event> healthCheck(String path) {
        String newHealthCheckURL = String
                .format("%s/admin/health", path);
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

    public HealthCheckResponse getStatus() {
        //return completeStatusMap;
        return null;
    }

    public boolean healthCheckOne(String path) {
        try {
            Mono<Event> monoEvent = healthCheck(path);
            monoEvent.block();
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }
}
