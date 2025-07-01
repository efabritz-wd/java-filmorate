package ru.yandex.practicum.filmorate.service.film;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    @Qualifier("dbFilmStorage")
    private final FilmStorage filmStorage;
    @Qualifier("dbUserStorage")
    private final UserStorage userStorage;

    public List<Film> findAllFilms() {
        return this.filmStorage.findAllFilms();
    }

    public Film findFilm(long id) throws FileNotFoundException {
        return this.filmStorage.findFilmById(id);
    }

    public Film createFilm(Film film) {
        return this.filmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        return this.filmStorage.updateFilm(film);
    }

    public void addLikeToFilm(int filmId, int userId) throws FileNotFoundException {
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
        log.info("Лайк фильму " + film.getId() + " добавлен пользователем " + user.getId());
    }

    public void deleteLike(int filmId, int userId) throws FileNotFoundException {
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
        System.out.println("service find films with most likes");
        List<Film> res = filmStorage.findAllFilms().stream()
                .sorted(Comparator.comparingInt(Film::getLikesAmount))
                .limit(count)
                .collect(Collectors.toList())
                .reversed();
        return res;
    }

    public List<Genre> findAllGenres() {
        return this.filmStorage.findAllGenres();
    }

    public Genre findGenreById(long id) {
        return this.filmStorage.findGenreById(id);
    }

    public List<MPA> findAllMPA() {
        return this.filmStorage.findAllMPAs();
    }

    public Optional<MPA> findMPAById(long id) {
        return this.filmStorage.findMPAById(id);
    }
}

