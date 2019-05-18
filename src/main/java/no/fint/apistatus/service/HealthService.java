package no.fint.apistatus.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.extern.slf4j.Slf4j;
import no.fint.apistatus.ApplicationConfig;
import no.fint.apistatus.WebClientHealth;
import no.fint.apistatus.model.ComponentConfiguration;
import no.fint.apistatus.model.HealthCheckProps;
import no.fint.apistatus.model.HealthCheckResponse;
import no.fint.event.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class HealthService {

    private final Multimap<String, HealthCheckResponse> completedHealthChecks = ArrayListMultimap.create();
    // TODO: 2019-05-18 Needs to be concurrent
    //private final ConcurrentMap<String, List<HealthCheckResponse>> healthChecks = new ConcurrentHashMap<>();

    @Autowired
    private WebClientHealth webClient;

    @Autowired
    private ComponentService componentService;

    @Autowired
    private ApplicationConfig config;


    @Scheduled(fixedRateString = "${fint.apistatus.healthcheck-rate-ms:180000}", initialDelay = 10000)
    public void healthCheckAll() {
        completedHealthChecks.clear();
        log.info("Running health checks...");
        List<Mono<Event>> events = componentService.getComponents()
                .stream()
                .flatMap(this::getComponents)
                .map(this::healthCheck)
                .collect(Collectors.toList());
        Flux.merge(events).collectList().block();
        log.info("End running health checks");
    }


    private Stream<HealthCheckProps> getComponents(ComponentConfiguration componentConfiguration) {
        Stream.Builder<HealthCheckProps> stream = Stream.builder();

        if (componentConfiguration.isInBeta()) {
            stream.add(new HealthCheckProps.Builder(config.getHealthBaseUrlTemplate())
                    .withEnvironment("beta")
                    .withName(componentConfiguration.getName())
                    .withPath(componentConfiguration.getPath())
                    .build()
            );
        }

        if (componentConfiguration.isInProduction()) {
            stream.add(new HealthCheckProps.Builder(config.getHealthBaseUrlTemplate())
                    .withEnvironment("api")
                    .withName(componentConfiguration.getName())
                    .withPath(componentConfiguration.getPath())
                    .build()
            );
        }

        return stream.build();
    }

    private Mono<Event> healthCheck(HealthCheckProps healthCheckProps) {
        log.info("Running health check on {}", healthCheckProps.getUrl());
        return webClient.get(healthCheckProps.getUrl())
                .bodyToMono(Event.class)
                .onErrorResume(e -> {
                    Event<String> errorEvent = new Event<>();
                    errorEvent.addData(e.getMessage());
                    errorEvent.addData(e.getClass().getSimpleName());
                    errorEvent.setSource(healthCheckProps.getName());
                    errorEvent.setTime(System.currentTimeMillis());
                    return Mono.just(errorEvent);
                })
                .doOnSuccess(healthResult -> {
                            completedHealthChecks.get(healthCheckProps.getEnvironment())
                                    .removeIf(e -> e.getPath().equals(healthCheckProps.getPath()));
                            completedHealthChecks.put(
                                    healthCheckProps.getEnvironment(),
                                    new HealthCheckResponse(healthCheckProps.getPath(), healthResult));
                        }
                );
    }

    public Optional<HealthCheckResponse> getHealthCheck(String path, String environment) {
        return completedHealthChecks.get(environment)
                .stream()
                .filter(o -> o.getPath().equals(path))
                .findFirst();

    }

    public Collection<HealthCheckResponse> getHealthCheckByEnvironment(String environment) {
        return completedHealthChecks.get(environment);
    }

    public Map<String, Collection<HealthCheckResponse>> getHealthChecks() {
        return completedHealthChecks.asMap();
    }

    public void healthCheckOne(String path, String environment) {
        try {
            log.info("Running single health checks...");
            healthCheck(new HealthCheckProps.Builder(config.getHealthBaseUrlTemplate())
                    .withEnvironment(environment)
                    .withPath(path)
                    .build()
            ).block();
            log.info("End running single health checks...");
        } catch (Throwable t) {
            log.error("Exception occurred during single health check", t);
            throw t;
        }
    }
}
