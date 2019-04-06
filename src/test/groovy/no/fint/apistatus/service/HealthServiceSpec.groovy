package no.fint.apistatus.service

import no.fint.apistatus.model.ComponentConfiguration
import no.fint.event.model.health.HealthStatus
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import spock.lang.Specification

class HealthServiceSpec extends Specification {

    private def server = new MockWebServer()

    private def successComponent = new ComponentConfiguration(
            name: "administrasjon-personal",
            port: 0,
            path: "/administrasjon/personal",
            assetPath: "/api/components/assets/administrasjon/personal"
    )
    private def failedComponent = new ComponentConfiguration(
            name: "a-failing-uri",
            port: 0,
            path: "/failing/uri",
            assetPath: "/api/components/assets/failing/uri"
    )

    private def componentService = Mock(ComponentService)

    def healthService = new HealthService(componentService: componentService,
            webClient: WebClient.create(server.url('/').toString()))

    def successResponse = new MockResponse().setBody(new ClassPathResource("healthcheckSuccess.json").getFile().text)
    def failResponse = new MockResponse().setBody(new ClassPathResource("healthcheckFailed.json").getFile().text)

    void cleanup() {
        server.shutdown()
    }

    def "Health check one component return last event"() {
        given:
        server.enqueue(successResponse)

        when:
        healthService.healthCheckOne('administrasjon/personal')
        def healthChecks = healthService.getHealthChecks()

        then:
        healthChecks.size() == 1
        healthChecks[0].apiBaseUrl == 'administrasjon/personal'
        healthChecks[0].event
    }

    def "Multiple health checks on same component returns last check only"() {
        given:
        server.enqueue(successResponse)
        server.enqueue(successResponse)

        when:
        healthService.healthCheckOne('administrasjon/personal')
        healthService.healthCheckOne('administrasjon/personal')
        def healthChecks = healthService.getHealthChecks()

        then:
        healthChecks.size() == 1
    }

    def "Run healthcheck on all components"() {
        given:
        server.enqueue(successResponse)

        when:
        healthService.healthCheckAll()
        def healthChecks = healthService.getHealthChecks()

        then:
        1 * componentService.getComponents() >> [successComponent]
        healthChecks.size() == 1
    }

    def "Check that an event is created when healthcheck fails and that it got timestamp"() {
        given:
        def healthService = new HealthService(baseUrl: server.url('/').toString(),
                componentService: componentServiceFailing)
        healthService.init()
        def jsonResponse = new ClassPathResource("healthcheckFailed.json").getFile().text
        server.enqueue(new MockResponse().addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).setBody(jsonResponse))

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
        def healthService = new HealthService(baseUrl: server.url('/').toString(),
                componentService: componentServiceSuccess)
        healthService.init()
        def jsonResponse = new ClassPathResource("healthcheckSuccess.json").getFile().text
        server.enqueue(new MockResponse().addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).setBody(jsonResponse))

        when:
        healthService.healthCheckOne("/administrasjon/personal")

        then:
        healthService.status.size() == 1
        healthService.status.values()[0].data[0]["status"] == HealthStatus.APPLICATION_HEALTHY.name()
        healthService.status.values()[0].data[1]["status"] == HealthStatus.RECEIVED_IN_CONSUMER_FROM_PROVIDER.name()
    }
}
