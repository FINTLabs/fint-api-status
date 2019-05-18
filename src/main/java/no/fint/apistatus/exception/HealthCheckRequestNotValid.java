package no.fint.apistatus.exception;

public class HealthCheckRequestNotValid extends RuntimeException {
    public HealthCheckRequestNotValid(String message) {
        super(message);
    }
}
