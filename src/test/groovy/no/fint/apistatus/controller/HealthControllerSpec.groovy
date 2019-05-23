package no.fint.apistatus.controller

import com.fasterxml.jackson.databind.ObjectMapper
import no.fint.apistatus.ApplicationConfig
import no.fint.apistatus.model.HealthCheckProps
import no.fint.apistatus.model.HealthCheckRequest
import no.fint.apistatus.model.HealthCheckRequestValidator
import no.fint.apistatus.model.HealthCheckResponse
import no.fint.apistatus.service.HealthRepository
import no.fint.apistatus.service.HealthService
import no.fint.event.model.Event
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import java.util.concurrent.ConcurrentHashMap

import static org.hamcrest.Matchers.equalTo
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup

class HealthControllerSpec extends Specification {
    private HealthService healthService
    private HealthRepository healthRepository
    private HealthCheckProps props
    private MockMvc mockMvc
    private Map<String, List<HealthCheckResponse>> responseMap

    void setup() {
        healthService = Mock()
        healthRepository = Mock()
        mockMvc = standaloneSetup(new HealthController(
                healthService: healthService,
                healthRepository: healthRepository,
                validator: new HealthCheckRequestValidator(
                        applicationConfig: new ApplicationConfig(environments: ['api', 'beta'])
                )
        ))
                .build()
        props = HealthCheckProps.builder()
        .healthBaseUrlTemplate('http://%s.test.no%s/test')
                .environment('api')
                .path('/test/test')
                .build()
        responseMap = new ConcurrentHashMap<>()
        responseMap.put('api', [new HealthCheckResponse(props, new Event())])
    }

    def "Post new health check"() {
        given:
        def request = new HealthCheckRequest('test/test', 'api')
        def json = new ObjectMapper().writeValueAsString(request)

        when:
        def result = mockMvc.perform(post('/api/healthcheck')
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .content(json))

        then:
        1 * healthService.healthCheckOne(_ as String, _ as String)
        result.andExpect(status().isOk())
    }

    def "Get all health checks"() {
        when:

        def result = mockMvc.perform(get('/api/healthcheck'))

        then:
        1 * healthRepository.getHealthChecks() >> responseMap
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.api[0].props.url', equalTo('http://api.test.no/test/test/test')))
    }

    def "Get health check for path"() {
        when:
        def result = mockMvc.perform(get('/api/healthcheck/api?path=/test'))

        then:
        1 * healthRepository.getHealthCheckByPath('/test', 'api') >> responseMap
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.api[0].props.url', equalTo('http://api.test.no/test/test/test')))

    }
}
