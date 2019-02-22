package no.fint.fintapistatus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

@Service
public class HealthService {

    public static final String healthCheckURL = "https://play-with-fint.felleskomponent.no/utdanning/timeplan/admin/health";

    @Value("${baseUrl:https://play-with-fint.felleskomponent.no/}")
    private String baseUrl;

    @Autowired
    private WebClient webClient;

    public String healthCheckAll(Map<String, LinkedList<String>> domenekart) {
        StringBuilder status = new StringBuilder();

        Set<String> domene1nokkel = domenekart.keySet();

        for (String hoveddomene : domene1nokkel) {
            LinkedList<String> underdomener = domenekart.get(hoveddomene);
            for (String underdomene : underdomener) {
                status.append(healthCheck(hoveddomene,underdomene));
            }
        }
        return status.toString();
    }

    public String healthCheck(String hoveddomene, String underdomene) {
        StringBuilder status = new StringBuilder();
        String nyHealthCheckURL = String
                .format("%s%s/%s/admin/health", baseUrl, hoveddomene, underdomene);
        status.append(String.format("%s %s %s", "<br><br>Nå tester vi:<br>" ,nyHealthCheckURL, "<br><br>"));
        webClient = WebClient.builder()
                .baseUrl(nyHealthCheckURL)
                .defaultHeader("x-client", "testbruker")
                .defaultHeader("x-org-id", "fint.health")
                .build();
        Mono<String> healthResult = webClient.get().exchange().block().bodyToMono(String.class);
        return status.append(healthResult.block()).toString();
    }
}
