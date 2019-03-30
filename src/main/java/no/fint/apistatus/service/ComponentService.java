package no.fint.apistatus.service;

import no.fint.apistatus.model.ComponentConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

import static no.fint.apistatus.ApplicationConfig.TargetService;
import static no.fint.apistatus.ApplicationConfig.TargetService.ServiceTypes.CONFIGURATION;

@Service
public class ComponentService {

    @Autowired
    @TargetService(CONFIGURATION)
    private WebClient webClient;

    public List<ComponentConfiguration> getComponents() {
        Flux<ComponentConfiguration> componentConfigurationFlux = webClient
                .get().uri("api/components/configurations")
                .retrieve().bodyToFlux(ComponentConfiguration.class);

        return componentConfigurationFlux.collectList().block(Duration.ofSeconds(15));
    }
}
