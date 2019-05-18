package no.fint.apistatus.model;

import no.fint.apistatus.ApplicationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class HealthCheckRequestValidator {

    @Autowired
    private ApplicationConfig applicationConfig;

    public boolean validate(HealthCheckRequest request) {

        if (!StringUtils.startsWithIgnoreCase(request.getPath(), "/")) {
            request.setPath("/" + request.getPath());
        }

        if (StringUtils.endsWithIgnoreCase(request.getPath(), "/")) {
            request.setPath(StringUtils.trimTrailingCharacter(request.getPath(), '/'));
        }

        return applicationConfig.getEnvironments()
                .stream()
                .filter(e -> e.equals(request.getEnvironment()))
                .count() == 1;
    }
}
