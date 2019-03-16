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

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@EnableScheduling
public class HealthService {
    private ConcurrentHashMap<String, Event> completeStatusMap = new ConcurrentHashMap<>();

    @Value("${baseUrl:https://play-with-fint.felleskomponent.no/}")
    private String baseUrl;

    private WebClient webClient;

    @Autowired
    private ComponentService componentService;

    @PostConstruct
    public void init(){
        webClient = WebClient.builder()
                .defaultHeader("x-client", "testbruker")
                .defaultHeader("x-org-id", "health.fintlabs.no")
                .baseUrl(baseUrl)
                .build();
    }
    /*
    Check application.properties for fixedRateString.
     */
    @Scheduled(fixedRateString = "${servercheck.time}", initialDelay = 10000)
        public void healthCheckAll() {
        List<Mono<Event>> listMono = componentService.getComponents()
                .stream().map(componentConfiguration ->
                healthCheck(componentConfiguration.getPath())).collect(Collectors.toList());
        Flux.merge(listMono).collectList().block();
    }

    //Makes and returns a healthcheck based on parameter path. Returns errorEvent if Event creation fails.
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

    public Map<String,Event> getStatus() {
        return completeStatusMap;
    }

    public boolean healthCheckOne(String path) {
        try {
            Mono<Event> monoEvent = healthCheck(path);
            monoEvent.block();
            return true;
        }catch (Throwable t){
            t.printStackTrace();
            return false;
        }
    }
}
