package no.fint.fintapistatus.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ComponentList {

    private List<ComponentConfiguration> componentConfigurations;

    public ComponentList() {
        this.componentConfigurations = new ArrayList<>();
    }
}
