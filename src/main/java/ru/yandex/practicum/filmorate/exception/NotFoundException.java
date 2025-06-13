package ru.yandex.practicum.filmorate.exception;

public class NotFoundException extends RuntimeException {
    private String sourceClass;

    public NotFoundException(String message, String sourceClass) {
        super(message);
        this.sourceClass = sourceClass;
    }

    public String getSourceClass() {
      return sourceClass;
    }
}
