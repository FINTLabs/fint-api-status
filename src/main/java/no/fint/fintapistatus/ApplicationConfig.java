package no.fint.fintapistatus;

import no.fint.event.model.health.HealthStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ApplicationConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.create();
    }

}
