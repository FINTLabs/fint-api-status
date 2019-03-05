package no.fint.fintapistatus.controller

import no.fint.fintapistatus.service.HealthService
import org.springframework.web.reactive.function.client.WebClient

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
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
}
