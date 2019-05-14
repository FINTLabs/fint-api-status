package no.fint.apistatus.model;

import lombok.Data;

@Data
public class ComponentConfiguration {
    private String name;
    private Integer port;
    private String path;
    private String assetPath;
    private boolean isInProduction;
    private boolean isInBeta;
}