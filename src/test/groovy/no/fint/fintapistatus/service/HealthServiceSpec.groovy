package no.fint.fintapistatus.service


import no.fint.event.model.health.HealthStatus
import no.fint.fintapistatus.model.ComponentConfiguration
import no.fint.fintapistatus.service.ComponentService
import no.fint.fintapistatus.service.HealthService
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import spock.lang.Specification

class HealthServiceSpec extends Specification {

    private def mockWebServer = new MockWebServer()
    private def componentServiceSuccess = Mock(ComponentService) {
        getComponents() >> [new ComponentConfiguration(
                name: "administrasjon-personal",
                port: 0,
                path: "/administrasjon/personal",
                assetPath: "/api/components/assets/administrasjon/personal")]
}
    private def componentServiceFailing = Mock(ComponentService) {
        getComponents() >> [new ComponentConfiguration(
                name: "a-failing-uri",
                port: 0,
                path: "/failing/uri",
                assetPath: "/api/components/assets/failing/uri")]
    }
    private def healthService = new HealthService(baseUrl: mockWebServer.url('/').toString(),
            webClient: WebClient.create())


    def "Check single component and return Mono<Event>-object"() {
        given:
        def healthService = new HealthService(baseUrl: "https://play-with-fint.felleskomponent.no/", webClient: WebClient.create())

        when:
        def healthCheck = healthService.healthCheck("administrasjon/personal")

        then:
        healthCheck
    }

    def "Run healthcheck on all components"() {
        given:
        def healthService = new HealthService(baseUrl: mockWebServer.url('/').toString(),
                componentService: componentServiceSuccess)
        healthService.init()
        def jsonResponse = new ClassPathResource("healthcheckSuccess.json").getFile().text
        mockWebServer.enqueue(new MockResponse().addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).setBody(jsonResponse))

        when:
        healthService.healthCheckAll()

        then:
        healthService.status.size() == 1
        healthService.status.values()[0].data[0]["status"] == HealthStatus.APPLICATION_HEALTHY.name()
        healthService.status.values()[0].data[1]["status"] == HealthStatus.RECEIVED_IN_CONSUMER_FROM_PROVIDER.name()
    }
    def "Check that an event is created when healthcheck fails and that it got timestamp"() {
        given:
        def healthService = new HealthService(baseUrl: mockWebServer.url('/').toString(),
                componentService: componentServiceFailing)
        healthService.init()
        def jsonResponse = new ClassPathResource("healthcheckFailed.json").getFile().text
        mockWebServer.enqueue(new MockResponse().addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).setBody(jsonResponse))

        when:
        healthService.healthCheckAll()

        then:
        healthService.status.size() == 1
        healthService.status.values()[0].time
        healthService.status.values()[0].data[0] != HealthStatus.APPLICATION_HEALTHY.name()
        healthService.status.values()[0].data[1] != HealthStatus.RECEIVED_IN_CONSUMER_FROM_PROVIDER.name()
    }
    def "Run healthcheck on a single path"() {
        given:
        def healthService = new HealthService(baseUrl: mockWebServer.url('/').toString(),
                componentService: componentServiceSuccess)
        healthService.init()
        def jsonResponse = new ClassPathResource("healthcheckSuccess.json").getFile().text
        mockWebServer.enqueue(new MockResponse().addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).setBody(jsonResponse))

        when:
        healthService.healthCheckOne("/administrasjon/personal")

        then:
        healthService.status.size() == 1
        healthService.status.values()[0].data[0]["status"] == HealthStatus.APPLICATION_HEALTHY.name()
        healthService.status.values()[0].data[1]["status"] == HealthStatus.RECEIVED_IN_CONSUMER_FROM_PROVIDER.name()
    }
}
