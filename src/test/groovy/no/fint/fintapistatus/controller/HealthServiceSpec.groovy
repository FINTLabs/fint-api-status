package no.fint.fintapistatus.controller

import no.fint.fintapistatus.service.ComponentService
import no.fint.fintapistatus.service.HealthService
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.web.reactive.function.client.WebClient
import spock.lang.Specification

class HealthServiceSpec extends Specification {

  private def mockWebServer = new MockWebServer()
  private def healthService = new HealthService(baseUrl: mockWebServer.url('/').toString(),
      webClient: WebClient.create())

  def "Health check given valid domains return application status"() {
    given:
    mockWebServer.enqueue(new MockResponse().setBody('{"status": "APPLICATION_HEALTHY"}'))

    when:
    def result = healthService.healthCheck('administrasjon', 'personal')
    def request = mockWebServer.takeRequest()

    then:
    request.path == '/administrasjon/personal/admin/health'
    //result.status =
    //result.corrID
  }
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
    def healthService = new HealthService(baseUrl: "https://play-with-fint.felleskomponent.no/",
            webClient: WebClient.create(),
            componentService: new ComponentService(webClient: WebClient.create(),
                    componentConfigurationUri: "https://admin.fintlabs.no/api/components/configurations"))

    when:
    healthService.healthCheckAll()

    then:
    healthService.statusLogs
  }
}
