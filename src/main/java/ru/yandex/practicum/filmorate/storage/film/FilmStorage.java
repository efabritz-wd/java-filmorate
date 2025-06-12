package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import java.util.List;

public interface FilmStorage {
    List<Film> findAllFilms();
    Film findFilmById(int id);
    Film createFilm(Film film);
    Film updateFilm(Film film);
    void deleteFilm(Film film);
}
