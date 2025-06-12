package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.film.FilmService;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> findAll() {
        return filmService.getFilmStorage().findAllFilms();
    }

    @GetMapping(value = "/{filmId}")
    public Film findUserById(@RequestParam int filmId) {
        return filmService.getFilmStorage().findFilmById(filmId);
    }

    @PostMapping
    public Film createFilm(@RequestBody Film film) {
        return filmService.getFilmStorage().createFilm(film);
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        return filmService.getFilmStorage().updateFilm(newFilm);
    }

    // PUT /films/{id}/like/{userId}
    @PutMapping(value = "/{id}/like/{userId}")
    public void setLikeToFilm(@RequestBody int id, int userId) {
        filmService.addLikeToFilm(id, userId);
    }

    // DELETE /films/{id}/like/{userId}
    @DeleteMapping(value = "/{id}/like/{userId}")
    public void deleteLikeToFilm(@RequestBody int id, int userId) {
        filmService.deleteLike(id, userId);
    }
    // GET /films/popular?count={count}
    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestBody int count) {
        if (count > 10) {
            count = 10;
        }
        return filmService.getFilmsWithMostLikes(count);
    }
}
