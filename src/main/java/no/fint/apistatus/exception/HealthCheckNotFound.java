package no.fint.apistatus.exception;

public class HealthCheckNotFound extends RuntimeException {
    public HealthCheckNotFound(String message) {
        super(message);
    }
}
