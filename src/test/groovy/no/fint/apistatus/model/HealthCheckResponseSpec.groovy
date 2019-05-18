package no.fint.apistatus.model


import no.fint.event.model.Event
import no.fint.event.model.health.HealthStatus
import spock.lang.Specification

class HealthCheckResponseSpec extends Specification {

    def "isHealthy is true on healthy event"() {
        given:
        def data = new ArrayList()
        def status = new LinkedHashMap();
        status.put("status", HealthStatus.APPLICATION_HEALTHY.toString())
        data.add(status)

        def event = new Event(data: data)

        when:
        def response = new HealthCheckResponse("/test/test", event)

        then:
        response.healthy
    }

    def "isHealthy is false on unhealthy event"() {
        given:
        def data = new ArrayList()
        data.add("error")

        def event = new Event(data: data)

        when:
        def response = new HealthCheckResponse("/test/test", event)

        then:
        !response.healthy
    }
}
