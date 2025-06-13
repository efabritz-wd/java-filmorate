package ru.yandex.practicum.filmorate.exception;

public class ValidationException extends RuntimeException {
    private String sourceClass;
    public ValidationException(String message, String sourceClass) {
        super(message);
        this.sourceClass = sourceClass;
    }

    public String getSourceClass() {
        return sourceClass;
    }
}
