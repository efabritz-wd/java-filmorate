package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    List<Film> findAllFilms();

    Film findFilmById(long id) throws FileNotFoundException;

    Film createFilm(Film film);

    Film updateFilm(Film film);

    void deleteFilm(Film film);

    List<Genre> findAllGenres();

    Genre findGenreById(long id);

    List<MPA> findAllMPA();

    Optional<MPA> findMPAById(long id);
}
