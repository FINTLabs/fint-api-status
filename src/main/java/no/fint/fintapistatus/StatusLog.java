package no.fint.fintapistatus;

import no.fint.event.model.Event;
import no.fint.fintapistatus.controller.HealthService;
import java.util.*;

public class StatusLog{

    LinkedList<Event> statusLog;

    public StatusLog(LinkedList<Event> statusLog) {
        this.statusLog = statusLog;
    }
    public StatusLog() {
        this.statusLog = new LinkedList<>();
    }

    public StatusLog(Event healthResult) {
        this.statusLog = new LinkedList<>();
        statusLog.add(healthResult);
    }

    public void add(Event healthResult) {
        this.statusLog.add(healthResult);
    }

    public Event getLastStatus() {
        return statusLog.get(statusLog.size()-1);
    }

    public Event getLastHealthyStatus(HealthService healthService) {
        LinkedList<Event> tempList = new LinkedList<>();
        for (Event event : statusLog){
            if (healthService.containsHealthyStatus(event)){
                tempList.add(event);
            }
        }
        return (!tempList.isEmpty()) ? tempList.getLast() : null;
    }

    public String getSource() {
        return (!this.statusLog.isEmpty()) ? statusLog.getFirst().getSource() : "Ingen Source";
    }
}