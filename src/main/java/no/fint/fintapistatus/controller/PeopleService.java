package no.fint.fintapistatus.controller;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PeopleService {
    public List<People> getAllPeople(){
        ArrayList<People> peoples = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            People newPerson = new People();
            newPerson.setFirstname(String.format("Fornavn%s", i));
            newPerson.setLastname(String.format("Etternavn%s", i));
            newPerson.setMobile(String.format("Mobilnummer%s", i));
            peoples.add(newPerson);
        }
        return peoples;
    }
}
