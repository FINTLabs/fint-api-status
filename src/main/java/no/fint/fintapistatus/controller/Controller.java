package no.fint.fintapistatus.controller;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api")
public class Controller {
    @Autowired
    private PeopleService peopleService;
    @Autowired
    private HealthService healthService;

    private TreeMap<String, LinkedList<String>> domenekart = new TreeMap<>();
    private String domene1 = "administrasjon";
    private String domene2 = "utdanning";
    private LinkedList<String> domene1Underdomener = new LinkedList<>();
    private LinkedList<String> domene2Underdomener = new LinkedList<>();

    @GetMapping(value = "/people")
    public List<People> getPeople() {
        return peopleService.getAllPeople();
    }

    @GetMapping(value = "/test/{var}")
    public String getVar(@PathVariable final String var) {
        return var;
    }

    @GetMapping(value = "/healthcheck")
    public String getHealthCheckStatus() {
        domene1Underdomener.add("personal");
        domene1Underdomener.add("organisasjon");
        domene1Underdomener.add("kodeverk");
        domenekart.put(domene1, domene1Underdomener);
        domene2Underdomener.add("elev");
        domene2Underdomener.add("utdanningsprogram");
        domene2Underdomener.add("vurdering");
        domene2Underdomener.add("timeplan");
        domene2Underdomener.add("kodeverk");
        domenekart.put(domene2, domene2Underdomener);
        return makeReadable(healthService.healthCheck(domenekart));
    }
    private String makeReadable(String status) {
        if (status != null && status.length() > 50) {
            status = status.replace("{", "{<br>");
            status = status.replace("}", "}<br>");
            System.out.println(status);
        }
        return status;
    }
}
