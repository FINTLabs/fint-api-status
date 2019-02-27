package no.fint.fintapistatus.controller;

import no.fint.event.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
public class HealthService {

    private static final String healthCheckURL = "https://play-with-fint.felleskomponent.no/utdanning/timeplan/admin/health";

    @Value("${baseUrl:https://play-with-fint.felleskomponent.no/}")
    private String baseUrl;

    @Autowired
    private WebClient webClient;


    public void healthCheckAll(Map<String, List<String>> domenekart) {
        Set<String> domainKeys = domenekart.keySet();
        for (String mainKey : domainKeys) {
            List<String> secondaryDomains = domenekart.get(mainKey);
            for (String secondaryDomain : secondaryDomains) {
                 healthCheck(mainKey,secondaryDomain);
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
        try{
            Event healthResult = clientResponse.bodyToMono(Event.class).block();
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
            if (containsHealthyStatus(healthResult)){
                Controller.lastHealthyStatus.put(healthResult.getSource(), healthResult);
            }
            if (Controller.statusLog.get(healthResult.getSource()) != null){
                Controller.statusLog.get(healthResult.getSource()).add(healthResult);
            }else{
                LinkedList<Event> newList = new LinkedList<>();
                newList.add(healthResult);
                Controller.statusLog.put(healthResult.getSource(), newList);
            }
        }
    }
    private boolean containsHealthyStatus(Event event) {
         final String APLICATION_HEALTHY = "APPLICATION_HEALTHY";
        if (event != null)
            if (event.getData()!=null){
                for (int i = 0; i <event.getData().size() ; i++) {
                    if (event.getData().get(i).toString().contains(APLICATION_HEALTHY))
                        return true;
                }
            }
        return false;

    }
}
