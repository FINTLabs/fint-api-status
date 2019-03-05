package no.fint.fintapistatus.service;

import no.fint.fintapistatus.model.ComponentConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class ComponentService {

    @Value("${fint.api.status.component.configuration.uri:https://admin.fintlabs.no/api/components/configurations}")
    private String componentConfigurationUri;

    @Autowired
    private WebClient webClient;

    public List<ComponentConfiguration> getComponents() {

        Flux<ComponentConfiguration> componentConfigurationFlux = webClient.get().uri(componentConfigurationUri).retrieve().bodyToFlux(ComponentConfiguration.class);

        return componentConfigurationFlux.collectList().block();
    }
}
