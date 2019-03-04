package no.fint.fintapistatus;

import no.fint.event.model.Event;
import no.fint.fintapistatus.controller.HealthService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class StatusLog {

    @Autowired
    private LinkedList<Event> statusLog;

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
        statusLog.add(healthResult); // Allways keep the last status checked.
        if (statusLog.size() >= 3) { // List must contain atleast 3 events before we decide to remove any.
            Event lastEventRegistered = statusLog.get(statusLog.size()-2); //Get the previous last event in the list
            Event secondLastEventRegistered = statusLog.get(statusLog.size()-3); // Get the event in front of previous last event.
            if (lastEventRegistered.getData() == secondLastEventRegistered.getData()) {//Compare the two events we found
                statusLog.remove(statusLog.size()-2);//Removing the event if it there are no changes.
            }
            if (statusLog.size() > 30) { // If the list contains more than 30 events, start a remove function that...
                for (int i = 0; i < 10; i++) { // ...removes the earliest 10 events, but keeps the first event.
                    statusLog.remove(1);
                }
            }
        }
    }

    public Event getLastStatus() {
        return statusLog.get(statusLog.size() - 1);
    }

    public Event getLastHealthyStatus(HealthService healthService) {
        LinkedList<Event> tempList = new LinkedList<>();
        for (Event event : statusLog) {
            if (healthService.containsHealthyStatus(event)) {
                tempList.add(event);
            }
        }
        return (!tempList.isEmpty()) ? tempList.getLast() : null;
    }

    public String getSource() {
        return (!this.statusLog.isEmpty()) ? statusLog.getFirst().getSource() : "Ingen Source";
    }

    public void skrivUt() {
        int teller = 1;
        for (Event event : this.statusLog){
            System.out.println("\nNr."+ teller +" - Source: " + event.getSource() +  "\nData: " + event.getData());
            teller++;
        }
    }
}