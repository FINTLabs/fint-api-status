package no.fint.apistatus.service

import no.fint.apistatus.model.HealthCheckProps
import no.fint.apistatus.model.HealthCheckResponse
import no.fint.event.model.Event
import spock.lang.Specification

class HealthRepositorySpec extends Specification {

    private HealthRepository repository
    private HealthCheckProps props
    private HealthCheckResponse response

    void setup() {
        repository = new HealthRepository()
        props = new HealthCheckProps.Builder('http://%s.test.no/%s/test')
                .withEnvironment('api')
                .withPath('/test/test')
                .build()
        response = new HealthCheckResponse(props, new Event())
    }

    def "Add health check response"() {
        when:
        repository.add(response)

        then:
        repository.getHealthChecks().size() == 1
        repository.getHealthCheckByEnvironment(response.getProps().getEnvironment()).size() == 1
    }

    def "Add health check response for new test on already tested health check"() {
        when:
        repository.add(response)
        repository.add(response)

        then:
        repository.getHealthChecks().size() == 1
        repository.getHealthCheckByEnvironment(response.getProps().getEnvironment()).size() == 1
    }
}
