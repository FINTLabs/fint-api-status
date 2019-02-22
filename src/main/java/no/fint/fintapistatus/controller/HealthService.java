package no.fint.fintapistatus.controller;

import no.fint.event.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class HealthService {

    public static final String healthCheckURL = "https://play-with-fint.felleskomponent.no/utdanning/timeplan/admin/health";

    @Value("${baseUrl:https://play-with-fint.felleskomponent.no/}")
    private String baseUrl;

    @Autowired
    private WebClient webClient;


    public void healthCheckAll(Map<String, LinkedList<String>> domenekart) {
        Set<String> domene1nokkel = domenekart.keySet();

        for (String hoveddomene : domene1nokkel) {
            LinkedList<String> underdomener = domenekart.get(hoveddomene);
            for (String underdomene : underdomener) {
                 healthCheck(hoveddomene,underdomene);
            }
        }
    }

    public void healthCheck(String hoveddomene, String underdomene) {
        String nyHealthCheckURL = String
                .format("%s%s/%s/admin/health", baseUrl, hoveddomene, underdomene);
        webClient = WebClient.builder()
                .baseUrl(nyHealthCheckURL)
                .defaultHeader("x-client", "testbruker")
                .defaultHeader("x-org-id", "fint.health")
                .build();
        ClientResponse clientResponse = webClient.get().exchange().block();
        Event healthResult = clientResponse.bodyToMono(Event.class).block();
        try{
            addHealthResultToLogg(healthResult);
        }catch (Exception e){
            String key = String.format("%s-%s",hoveddomene,underdomene);
            Event errorEvent = new Event();
            errorEvent.setSource(key);
            LinkedList<String> s = new LinkedList<>();
            s.add(e.getMessage());
            s.add(e.toString());
            errorEvent.setData(s);
            addHealthResultToLogg(errorEvent);
        }


    }

    private void addHealthResultToLogg(Event healthResult) {
        if (healthResult != null){
            if (Controller.containsHealthyStatus(healthResult)){
                Controller.lastHealthyStatus.put(healthResult.getSource(), healthResult);
            }
            if (Controller.healthStatusLogg.get(healthResult.getSource()) != null){
                Controller.healthStatusLogg.get(healthResult.getSource()).add(healthResult);
            }else{
                LinkedList<Event> newList = new LinkedList<>();
                newList.add(healthResult);
                Controller.healthStatusLogg.put(healthResult.getSource(), newList);
            }
        }
    }
}
