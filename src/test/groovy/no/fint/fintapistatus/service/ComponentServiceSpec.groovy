package no.fint.fintapistatus.service

import com.fasterxml.jackson.databind.ObjectMapper
import no.fint.fintapistatus.model.ComponentConfiguration
import org.springframework.web.reactive.function.client.WebClient
import spock.lang.Specification

class ComponentServiceSpec extends Specification {

    def "Get component configurations"() {

        given:
        def componentService = new ComponentService(componentConfigurationUri: "https://admin.fintlabs.no/", webClient: WebClient.create())
        componentService.init()
        when:
        def components = componentService.getComponents()

        then:
        components.size() > 0
        components.every{it.name != null}
    }

    def "Name"() {
        given:
        def om = new ObjectMapper()

        when:
        def value = om.readValue("{\n" +
                "    \"name\": \"administrasjon-okonomi\",\n" +
                "    \"port\": 8290,\n" +
                "    \"path\": \"/administrasjon/okonomi\",\n" +
                "    \"assetPath\": \"/api/components/assets/administrasjon_okonomi\"\n" +
                "  }", ComponentConfiguration)

        then:
        value.name
    }
}
