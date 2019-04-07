package no.fint.apistatus.service

import no.fint.apistatus.model.ComponentConfiguration
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

    def successResponse = new MockResponse()
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(new ClassPathResource("healthcheckSuccess.json").getFile().text)
    def failResponse = new MockResponse()
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(new ClassPathResource("healthcheckFailed.json").getFile().text)

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
        healthChecks[0].event.data[0].status == 'APPLICATION_HEALTHY'
        healthChecks[0].event.data[1].status == 'RECEIVED_IN_CONSUMER_FROM_PROVIDER'
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

    def "Event with timestamp is created when health check fails"() {
        given:
        server.enqueue(failResponse)

        when:
        healthService.healthCheckAll()
        def healthChecks = healthService.getHealthChecks()

        then:
        1 * componentService.getComponents() >> [failedComponent]
        healthChecks.size() == 1
        healthChecks[0].event.time
    }
}
