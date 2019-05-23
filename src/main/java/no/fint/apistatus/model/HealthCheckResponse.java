package no.fint.apistatus.model;

import lombok.Getter;
import no.fint.event.model.Event;
import no.fint.event.model.health.HealthStatus;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
public class HealthCheckResponse {
    private HealthCheckProps props;
    private boolean isHealthy;
    private Event event;

    public HealthCheckResponse(HealthCheckProps props, Event event) {
        this.props = props;
        this.event = event;
        this.isHealthy = calculateHealth();
    }

    private boolean calculateHealth() {
        if (event.getData().size() > 0 && event.getData().get(0) instanceof Map) {
            List<LinkedHashMap<String, String>> data = event.getData();
            return data.stream().anyMatch(e -> e.get("status").equals(HealthStatus.APPLICATION_HEALTHY.name()));
        }
        return false;
    }
}
