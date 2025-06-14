package ru.yandex.practicum.filmorate.service.film;

import org.springframework.stereotype.Service;

import lombok.Getter;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public List<Film> findAllFilms() {
        return this.filmStorage.findAllFilms();
    }

    public Film findFilm(long id) {
        return this.filmStorage.findFilmById(id);
    }

    public Film createFilm(Film film) {
        return this.filmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        return this.filmStorage.updateFilm(film);
    }

    public void addLikeToFilm(int filmId, int userId) {
        Film film = this.filmStorage.findFilmById(filmId);
        User user = this.userStorage.findUserById(userId);
        if (film.getLikes().isEmpty()) {
            Set<Long> likesSet = new HashSet<>();
            likesSet.add(user.getId());
            film.setLikes(likesSet);
        } else {
            Set<Long> likeSet = film.getLikes();
            likeSet.add(user.getId());
            film.setLikes(likeSet);
        }
        this.filmStorage.updateFilm(film);
        log.info("Лайк добавлен");
    }

    public void deleteLike(int filmId, int userId) {
        Film film = this.filmStorage.findFilmById(filmId);
        User user = this.userStorage.findUserById(userId);
        if (!film.getLikes().contains(user.getId())) {
            throw new ConditionsNotMetException("Ошибка при удалении лайка.");
        } else {
            Set<Long> likeSet = film.getLikes();
            likeSet.remove(user.getId());
            user.setFriends(likeSet);
        }
        this.filmStorage.updateFilm(film);
        log.info("Лайк удален");
    }

    public List<Film> getFilmsWithMostLikes(int count) {
        List<Film> res = filmStorage.findAllFilms().stream()
                .sorted(Comparator.comparingInt(Film::getLikesAmount))
                .limit(count)
                .collect(Collectors.toList())
                .reversed();
        return res;
    }
}

