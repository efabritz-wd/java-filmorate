package ru.yandex.practicum.filmorate.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@RestControllerAdvice
public class ExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse validationExceptionUser(final UserValidationException e) {
        return new ErrorResponse(
                "Ошибка валидации пользователя",
                e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse validationExceptionFilm(final UserValidationException e) {
        return new ErrorResponse(
                "Ошибка валидации фильма",
                e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse notFoundExceptionUser(final UserNotFoundException e) {
        return new ErrorResponse(
                "Пользователь не найден",
                e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse notFoundExceptionFilm(final FilmNotFound e) {
        return new ErrorResponse(
                "Фильм не найден",
                e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse conditionsNotMetException(final ConditionsNotMetException e) {
        return new ErrorResponse(
                "Ошибка несоответствия параметров",
                e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse commonException(final RuntimeException e) {
        return new ErrorResponse(
                "Ошибка",
                e.getMessage()
        );
    }

}