package no.fint.fintapistatus.service;

import no.fint.fintapistatus.model.ComponentConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;

@Service
public class ComponentService {

    @Value("${fintlabs.admin.url:https://admin.fintlabs.no/}")
    private String componentConfigurationUri;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        webClient = WebClient.builder().baseUrl(componentConfigurationUri).build();
    }

    public List<ComponentConfiguration> getComponents() {

        Flux<ComponentConfiguration> componentConfigurationFlux = webClient
                .get().uri("api/components/configurations")
                .retrieve().bodyToFlux(ComponentConfiguration.class);

        return componentConfigurationFlux.collectList().block(Duration.ofSeconds(15));
    }
}
