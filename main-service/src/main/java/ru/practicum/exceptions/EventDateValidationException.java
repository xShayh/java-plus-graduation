package ru.practicum.exceptions;

public class EventDateValidationException extends RuntimeException {
    public EventDateValidationException(String message) {
        super(message);
    }
}
