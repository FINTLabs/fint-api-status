package no.fint.apistatus;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import static no.fint.apistatus.ApplicationConfig.TargetService.ServiceTypes.CONFIGURATION;
import static no.fint.apistatus.ApplicationConfig.TargetService.ServiceTypes.HEALTH;

@Configuration
public class ApplicationConfig {

    @Value("${fint.apistatus.client-header:testbruker}")
    private String clientHeader;

    @Value("${fint.apistatus.orgid-header:health.fintlabs.no}")
    private String orgIdHeader;

    @Getter
    @Value("${fint.apistatus.health-base-url-template:https://%s.felleskomponent.no%s/admin/health}")
    private String healthBaseUrlTemplate;

    @Getter
    @Value("${fint.apistatus.environments:api,beta,play-with-fint}")
    private List<String> environments;

    @Value("${fint.apistatus.configuration-base-url:https://admin.fintlabs.no}")
    private String configurationBaseUrl;

    @Bean
    @TargetService(HEALTH)
    public WebClient healthWebClient() {

        return WebClient.builder()
                .defaultHeader("x-client", clientHeader)
                .defaultHeader("x-org-id", orgIdHeader)
                .build();
    }


    @Bean
    @TargetService(CONFIGURATION)
    public WebClient configurationWebClient() {
        return WebClient.builder()
                .baseUrl(configurationBaseUrl)
                .build();
    }


    @Target({ElementType.METHOD, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier
    public @interface TargetService {

        ServiceTypes value();

        enum ServiceTypes {
            HEALTH, CONFIGURATION
        }
    }
}
