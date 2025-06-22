package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> filmsMap = new HashMap<>();
    public static final LocalDate boundaryReleaseDate = LocalDate.of(1895, 12, 28);

    @Override
    public List<Film> findAllFilms() {
        if (filmsMap.values().isEmpty()) {
            return new ArrayList<>();
        }

        return new ArrayList<>(filmsMap.values());
    }

    @Override
    public Film findFilmById(long id) {
        if (!filmsMap.containsKey(id)) {
            throw new NotFoundException("Фильма с id " + id + " не существует", Film.class.getName());
        }
        return filmsMap.get(id);
    }

    @Override
    public Film createFilm(Film film) {
        if (!validateFilm(film, true)) {
            throw new ValidationException("Ошибка валидации фильма при создании", Film.class.getName());
        }
        film.setId(getNextId());
        filmsMap.put(film.getId(), film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (film.getId() == null) {
            log.error("Обновление фильма: не указан id");
            throw new NotFoundException("Id должен быть указан", Film.class.getName());
        }
        if (!filmsMap.containsKey(film.getId())) {
            log.error("Обновление фильма: Фильм не найден");
            throw new NotFoundException("Фильма с id " + film.getId() + " не существует", Film.class.getName());
        }
        if (!validateFilm(film, false)) {
            throw new ValidationException("Ошибка валидации фильма при обновлении", Film.class.getName());
        }

        Film oldFilm = filmsMap.get(film.getId());

        if (film.getName() != null) {
            log.info("Обновление названия фильма");
            oldFilm.setName(film.getName());
        }
        if (film.getDescription() != null) {
            log.info("Обновление описания фильма");
            oldFilm.setDescription(film.getDescription());
        }
        if (film.getReleaseDate() != null) {
            log.info("Обновление даты выпуска фильма");
            oldFilm.setReleaseDate(film.getReleaseDate());
        }
        if (film.getDuration() != null) {
            log.info("Обновление продолжительности фильма");
            oldFilm.setDuration(film.getDuration());
        }
        if (!film.getLikes().isEmpty()) {
            log.info("Обновление лайков фильма");
            oldFilm.setLikes(film.getLikes());
        }

        filmsMap.put(film.getId(), oldFilm);
        return oldFilm;
    }

    @Override
    public void deleteFilm(Film film) {
        if (!filmsMap.keySet().contains(film.getId())) {
            throw new NotFoundException("Фильма с id " + film.getId() + " не существует", Film.class.getName());
        }
        filmsMap.remove(film.getId());
        log.info("Фильм с id " + film.getId() + " удален.");
    }

    private long getNextId() {
        long currentMaxId = filmsMap.keySet()
                .stream()
                .mapToLong(id -> id)
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
            return true;
        }
    }

}
