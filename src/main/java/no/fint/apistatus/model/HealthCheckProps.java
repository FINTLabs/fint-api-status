package no.fint.apistatus.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HealthCheckProps {

    String path;

    String environment;

    String name;

    @JsonIgnore
    String healthBaseUrlTemplate;

    private HealthCheckProps() {
    }

    public String getUrl() {
        return String.format(healthBaseUrlTemplate, environment, path);
    }

    public static class Builder {
        String path;
        String environment;
        String name;

        String healthBaseUrlTemplate;

        public Builder(String healthBaseUrlTemplate) {
            this.healthBaseUrlTemplate = healthBaseUrlTemplate;
        }

        public Builder withPath(String path) {
            this.path = path;
            return this;
        }

        public Builder withEnvironment(String environment) {
            this.environment = environment;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public HealthCheckProps build() {
            HealthCheckProps healthCheckProps = new HealthCheckProps();
            healthCheckProps.setEnvironment(this.environment);
            healthCheckProps.setHealthBaseUrlTemplate(this.healthBaseUrlTemplate);
            healthCheckProps.setName(this.name);
            healthCheckProps.setPath(this.path);

            return healthCheckProps;
        }

    }
}