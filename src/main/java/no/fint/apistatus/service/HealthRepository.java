package no.fint.apistatus.service;

import lombok.Data;
import no.fint.apistatus.model.HealthCheckResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Data
@Component
public class HealthRepository {

    private final ConcurrentMap<String, List<HealthCheckResponse>> healthChecks = new ConcurrentHashMap<>();

    public void add(HealthCheckResponse response) {
        createEnvironmentIfNotExists(response.getProps().getEnvironment());
        removeResponseIfExists(response);
        healthChecks.get(response.getProps().getEnvironment()).add(response);
    }

    public List<HealthCheckResponse> getHealthCheckByEnvironment(String environment) {
        return healthChecks.get(environment);
    }

    public Optional<HealthCheckResponse> getHealthCheckByPath(String path, String environment) {
        return healthChecks.get(environment)
                .stream()
                .filter(o -> o.getProps().getPath().equals(path))
                .findFirst();
    }

    private void removeResponseIfExists(HealthCheckResponse response) {
        healthChecks.get(response.getProps().getEnvironment())
                .removeIf(e -> e.getProps().getPath().equals(response.getProps().getPath()));
    }

    private void createEnvironmentIfNotExists(String environment) {
        if (!healthChecks.containsKey(environment)) {
            healthChecks.put(environment, Collections.synchronizedList(new ArrayList<>()));
        }
    }
}
