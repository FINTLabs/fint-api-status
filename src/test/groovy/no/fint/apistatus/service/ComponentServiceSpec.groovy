package no.fint.apistatus.service


import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import spock.lang.Specification

class ComponentServiceSpec extends Specification {

    private def server = new MockWebServer()

    private def componentService = new ComponentService(webClient: WebClient.create(server.url('/').toString()))
    private def jsonResponse = new ClassPathResource('componentConfigurations.json').getFile().text

    void cleanup() {
        server.shutdown()
    }

    def "Get component configurations"() {
        given:
        server.enqueue(new MockResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).setBody(jsonResponse))

        when:
        def components = componentService.getComponents()

        then:
        components.size() == 2
    }
}
