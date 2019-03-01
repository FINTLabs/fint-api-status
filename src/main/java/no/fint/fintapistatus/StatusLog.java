package no.fint.fintapistatus;

import no.fint.event.model.Event;
import no.fint.fintapistatus.controller.HealthService;

import java.util.*;

public class StatusLog{

    List<Event> statusLog;

    public StatusLog(List<Event> statusLog) {
        this.statusLog = statusLog;
    }
    public StatusLog() {
        this.statusLog = new ArrayList<>();
    }

    public StatusLog(Event healthResult) {
        ArrayList<Event> tempList = new ArrayList<>();
        statusLog = tempList;
        statusLog.add(healthResult);

    }

    public void add(Event healthResult) {
        this.statusLog.add(healthResult);
    }

    public Event getLastStatus() {
        return statusLog.get(statusLog.size()-1);
    }

    public String getLastHealthyStatus(HealthService healthService) {
        LinkedList<Event> tempList = new LinkedList<>();
        for (Event event : statusLog){
            if (healthService.containsHealthyStatus(event)){
                tempList.add(event);
            }
        }
        return (!tempList.isEmpty()) ? skrivUtEvent(tempList.getLast()) : "Ingen healthy statuser";
    }
    private String skrivUtEvent(Event event){
        return (event!=null) ? String.format("Source: %s <br>Data: %s <br>Message: %s",event.getSource(), event.getData().toString(), event.getMessage()):
                "Tomt event!";
    }
}