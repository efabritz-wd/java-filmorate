package ru.yandex.practicum.filmorate.exception;

public class FilmNotFound extends RuntimeException {
    public FilmNotFound(String message) {
        super(message);
    }
}
