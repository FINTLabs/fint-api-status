package no.fint.apistatus.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class HealthCheckProps {

    String path;

    String environment;

    String name;

    @JsonIgnore
    String healthBaseUrlTemplate;


    public String getUrl() {
        return String.format(healthBaseUrlTemplate, environment, path);
    }
}