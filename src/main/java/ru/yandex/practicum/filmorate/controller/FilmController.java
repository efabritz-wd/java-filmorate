package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.FilmNotFound;
import ru.yandex.practicum.filmorate.exception.FilmValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    public static final LocalDate boundaryReleaseDate = LocalDate.of(1895, 12, 28);

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    public static boolean validateFilm(Film film, boolean creation) {
        if (creation) {
            if (film.getName() == null || film.getDescription() == null
                    || film.getReleaseDate() == null || film.getDuration() == null) {
                log.error("Ошибка валидации при создании фильма: одно из полей отсутствует");
                return false;
            }

            if (!film.getName().isBlank() && film.getDescription().length() <= 200
                    && film.getReleaseDate().isAfter(boundaryReleaseDate) && (film.getDuration() > 0)) {
                log.info("Валидация фильма прошла успешно");
                return true;
            } else {
                log.error("Ошибка валидации при создании фильма: одно из полей неверно заполнено");
                return false;
            }
        } else {
            if (film.getName() != null && film.getName().isBlank()) {
                log.error("Ошибка валидации при обновлении фильма: имя пустое");
                return false;
            }
            if (film.getDescription() != null && (film.getDescription().length() > 200)) {
                log.error("Ошибка валидации при обновлении фильма: описание больше 2000 символов");
                return false;
            }
            if (film.getReleaseDate() != null && (film.getReleaseDate().isBefore(boundaryReleaseDate))) {
                log.error("Ошибка валидации при обновлении фильма: дата выпуска до первого фильма");
                return false;
            }
            if (film.getDuration() != null && (film.getDuration() < 0)) {
                log.error("Ошибка валидации при обновлении фильма: продолжительность отрицательная");
                return false;
            }
        }
        return false;
    }

    @PostMapping
    public Film createFilm(@RequestBody Film film) {
        if (!validateFilm(film, true)) {
            throw new FilmValidationException("Фильм не прошел валидацию при добавлении");
        }
        log.info("Новый фильм прошел валидацию. Установка значений полей");
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Новый фильм добавлен");
        return film;
    }

    private int getNextId() {
        int currentMaxId = films.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        if (newFilm.getId() == null) {
            log.error("Фильма с таким id не существует");
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        if (validateFilm(newFilm, false)) {
            throw new FilmValidationException("Фильм не прошел валидацию при обновлении");
        }
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());

            if (newFilm.getName() != null) {
                log.info("Обновление названия фильма");
                oldFilm.setName(newFilm.getName());
            }
            if (newFilm.getDescription() != null) {
                log.info("Обновление описания фильма");
                oldFilm.setDescription(newFilm.getDescription());
            }
            if (newFilm.getReleaseDate() != null) {
                log.info("Обновление даты выпуска фильма");
                oldFilm.setReleaseDate(newFilm.getReleaseDate());
            }
            if (newFilm.getDuration() != null) {
                log.info("Обновление продолжительности фильма");
                oldFilm.setDuration(newFilm.getDuration());
            }

            return oldFilm;
        } else {
            log.error("Обновление фильма: Фильм не найден");
            throw new FilmNotFound("Фильм с id: " + newFilm.getId() + " не найден");
        }
    }
}
