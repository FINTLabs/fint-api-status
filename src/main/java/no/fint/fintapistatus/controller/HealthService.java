package no.fint.fintapistatus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class HealthService {

    public static final String healthCheckURL = "https://play-with-fint.felleskomponent.no/utdanning/timeplan/admin/health";
    @Autowired
    private WebClient webClient;

    public String healthCheck(){
        String status;
        webClient = WebClient.builder()
                .baseUrl("https://play-with-fint.felleskomponent.no/utdanning/timeplan/admin/health")
                .defaultHeader("x-client", "testbruker")
                .defaultHeader("x-org-id", "health.fintlabs.no").build();
        Mono<String> test = webClient.get().exchange().block().bodyToMono(String.class);
        status = test.block();
        return status;
    }
}
