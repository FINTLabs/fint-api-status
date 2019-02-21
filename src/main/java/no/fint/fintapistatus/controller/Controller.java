package no.fint.fintapistatus.controller;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping(value = "/healthcheck/all")
    public String getHealthCheckStatusAll() {
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
        return makeReadable(healthService.healthCheckAll(domenekart));
    }
    @GetMapping(value = "/healthcheckwithdomene/{domene}/{underdomene}")
    public String getHealthCheckStatusByDomene(
            @PathVariable("domene") final String domene,
            @PathVariable("underdomene") final String underdomene) {
        return makeReadable(healthService.healthCheck(domene, underdomene));
    }
    private String makeReadable(String status) {
        if (status != null && status.length() > 50) {
            status = status.replace("{", "{<br>");
            status = status.replace("}", "}<br>");
            status = status.replace("[", "<blockquote>[");
            status = status.replace("]", "]</blockquote>");
            status = status.replace("No response from adapter", "<b>No response from adapter</b>");
            status = status.replace("APPLICATION_HEALTHY", "<b>APPLICATION_HEALTHY</b>");
            System.out.println(status);
        }
        return status;
    }
}
