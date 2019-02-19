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
    WebClient webClient;
            /*WebClient
            .builder()
            .baseUrl("https://play-with-fint.felleskomponent.no/utdanning/timeplan/admin/health")
            .defaultHeader("x-org-id=health.fintlabs.no").build();*/

    public String healthCheck(){
        String status;
        Mono<String> test = webClient.get()
                .uri(healthCheckURL)
                .header("x-org-id", "health.fintlabs.no " + Base64Utils
                        .encodeToString(("").getBytes(UTF_8)))
                .retrieve().bodyToMono(String.class);
        status = test.block();
        return status;
    }
}
