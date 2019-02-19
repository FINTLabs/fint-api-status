package no.fint.fintapistatus.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/api/")
public class Controller {
@Autowired
private PeopleService peopleService;
@Autowired
HealthService healthService;

    @GetMapping(value = "/people")
    public List<People> getPeople(){
        return peopleService.getAllPeople();
    }

    @GetMapping(value = "/test/{var}")
    public String getVar(@PathVariable final String var){


        return var;
    }
    @GetMapping(value = "/healthcheck")
    public String getHealthCheckStatus(){
        String status = healthService.healthCheck();
        return status;
    }
}
