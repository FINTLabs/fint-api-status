package no.fint.fintapistatus.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.fint.event.model.Event;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheckResponse {
    private String apiBaseUrl;
    private Event event;
}
