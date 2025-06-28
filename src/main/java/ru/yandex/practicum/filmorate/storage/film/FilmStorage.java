package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;

import java.util.List;

public interface FilmStorage {
    List<Film> findAllFilms();

    Film findFilmById(long id);

    Film createFilm(Film film);

    Film updateFilm(Film film);

    void deleteFilm(Film film);

    List<Genre> findAllGenres();

    Genre findGenreById(long id);

    List<MPA> findAllMPA();

    MPA findMPAById(long id);
}
