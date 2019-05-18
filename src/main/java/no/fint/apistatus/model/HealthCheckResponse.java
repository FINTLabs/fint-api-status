package no.fint.apistatus.model;

import lombok.Getter;
import no.fint.event.model.Event;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
public class HealthCheckResponse {
    private String path;
    private boolean isHealthy;
    private Event event;

    public HealthCheckResponse(String path, Event event) {
        this.path = path;
        this.event = event;
        this.isHealthy = calculateHealth();
    }

    private boolean calculateHealth() {
        if (event.getData().size() > 0 && event.getData().get(0) instanceof Map) {
            List<LinkedHashMap<String, String>> data = event.getData();
            return data.stream().anyMatch(e -> e.get("status").equals("APPLICATION_HEALTHY"));
        }
        return false;
    }
}
