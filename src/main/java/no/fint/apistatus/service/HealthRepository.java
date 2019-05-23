package no.fint.apistatus.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import lombok.Data;
import no.fint.apistatus.model.HealthCheckResponse;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class HealthRepository {

    private final Multimap<String, HealthCheckResponse> healthChecks = Multimaps.synchronizedListMultimap(ArrayListMultimap.create());


    public void add(HealthCheckResponse response) {
        removeResponseIfExists(response);
        healthChecks.put(response.getProps().getEnvironment(), response);
    }

    public Map<String, Collection<HealthCheckResponse>> getHealthChecks() {
        return healthChecks.asMap();
    }

    public Collection<HealthCheckResponse> getHealthCheckByEnvironment(String environment) {
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
}
