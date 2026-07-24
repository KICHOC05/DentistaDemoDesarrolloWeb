package com.dnt.appointment.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, String field, String value) {
        super(String.format("%s no encontrado con %s: %s", resource, field, value));
    }
}
