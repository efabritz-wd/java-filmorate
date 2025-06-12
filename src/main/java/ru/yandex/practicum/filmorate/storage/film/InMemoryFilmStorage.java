package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFound;
import ru.yandex.practicum.filmorate.exception.FilmValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> filmsMap = new HashMap<>();
    public static final LocalDate boundaryReleaseDate = LocalDate.of(1895, 12, 28);

    @Override
    public List<Film> findAllFilms() {
        if (filmsMap.values().isEmpty()) {
            return List.of();
        }

        return (List<Film>) filmsMap.values();
    }

    @Override
    public Film findFilmById(int id) {
        if (!filmsMap.containsKey(id)) {
            throw new FilmNotFound("Фильма с id " + id + " не существует");
        }
        return filmsMap.get(id);
    }

    @Override
    public Film createFilm(Film film) {
        if (!validateFilm(film, true)) {
            throw new FilmValidationException("Ошибка валидации фильма при создании");
        }
        film.setId(getNextId());
        filmsMap.put(film.getId(), film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (!validateFilm(film, false)) {
            throw new FilmValidationException("Ошибка валидации фильма при обновлении");
        }
        if (!filmsMap.keySet().contains(film.getId())) {
            throw new FilmNotFound("Фильма с id " + film.getId() + " не существует");
        }
        filmsMap.put(film.getId(), film);
        return film;
    }

    @Override
    public void deleteFilm(Film film) {
        if (!filmsMap.keySet().contains(film.getId())) {
            throw new FilmNotFound("Фильма с id " + film.getId() + " не существует");
        }
        filmsMap.remove(film.getId());
        log.info("Фильм с id " + film.getId() + " удален.");
    }

    private int getNextId() {
        int currentMaxId = filmsMap.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
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

}
