package no.fint.apistatus.model

import no.fint.apistatus.ApplicationConfig
import spock.lang.Specification

class HealthCheckRequestValidatorSpec extends Specification {

    private ApplicationConfig config
    private HealthCheckRequestValidator validator

    void setup() {
        config = new ApplicationConfig(environments: ["api", "beta"])
        validator = new HealthCheckRequestValidator(applicationConfig: config)
    }

    def "Validate valid health check request"() {
        given:
        def request = new HealthCheckRequest(path: "administrasjon/person/", environment: "api")

        when:
        def valid = validator.validate(request)

        then:
        valid
        request.path == "/administrasjon/person"
    }

    def "Validate health check request with unvalid environment"() {
        when:
        def valid = validator.validate(new HealthCheckRequest(path: "administrasjon/person/", environment: "tjohei"))

        then:
        !valid
    }
}
