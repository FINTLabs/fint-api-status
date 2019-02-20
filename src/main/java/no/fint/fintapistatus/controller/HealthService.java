package no.fint.fintapistatus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

@Service
public class HealthService {

    public static final String healthCheckURL = "https://play-with-fint.felleskomponent.no/utdanning/timeplan/admin/health";
    @Autowired
    private WebClient webClient;

    public String healthCheck(Map<String, LinkedList<String>> domenekart){
        StringBuilder status = new StringBuilder();

        Set<String> domene1nøkkel = domenekart.keySet();

        for (String hoveddomene : domene1nøkkel) {
            LinkedList<String> underdomener = domenekart.get(hoveddomene);
            for (String underdomene : underdomener){
                String nyHealthCheckURL = String
                        .format("https://play-with-fint.felleskomponent.no/%s/%s/admin/health", hoveddomene,underdomene);
                status.append("<br><br>Nå tester vi:<br>"+nyHealthCheckURL+ "<br><br>");
                webClient = WebClient.builder()
                        .baseUrl(nyHealthCheckURL)
                        .defaultHeader("x-client", "testbruker")
                        .defaultHeader("x-org-id", "fint.health")
                        .build();
                Mono<String> test = webClient.get().exchange().block().bodyToMono(String.class);
                status.append(test.block());
            }
        }
        return status.toString();
    }
}
