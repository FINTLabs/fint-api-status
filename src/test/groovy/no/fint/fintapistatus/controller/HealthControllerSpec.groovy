package no.fint.fintapistatus.controller

import com.fasterxml.jackson.databind.ObjectMapper
import no.fint.event.model.Event
import no.fint.fintapistatus.model.HealthCheckRequest
import no.fint.fintapistatus.model.HealthCheckResponse
import no.fint.fintapistatus.service.HealthService
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.hamcrest.Matchers.equalTo
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup

class HealthControllerSpec extends Specification {
    private HealthService healthService
    private MockMvc mockMvc

    void setup() {
        healthService = Mock()
        mockMvc = standaloneSetup(new HealthController(healthService: healthService)).build()
    }

    def "Post new health check"() {
        given:
        def request = new HealthCheckRequest("http://localhost")
        def json = new ObjectMapper().writeValueAsString(request)

        when:
        def result = mockMvc.perform(post('/api/healthcheck')
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .content(json))

        then:
        1 * healthService.healthCheckOne(request.apiBaseUrl)
        result.andExpect(status().isOk())
    }

    def "Get health"() {
        when:
        def result = mockMvc.perform(get('/api/healthcheck'))

        then:
        1 * healthService.getStatus() >> new HealthCheckResponse('http://localhost', new Event())
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.apiBaseUrl', equalTo('http://localhost')))
    }
}
