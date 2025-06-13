package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.film.FilmService;
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
    public List<Film> findAll() {
        return filmService.getFilmStorage().findAllFilms();
    }

    @GetMapping(value = "/{filmId}")
    public Film findUserById(@PathVariable int filmId) {
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

    @PutMapping(value = "/{id}/like/{userId}")
    public void setLikeToFilm(@PathVariable int id, @PathVariable int userId) {
        filmService.addLikeToFilm(id, userId);
    }

    @DeleteMapping(value = "/{id}/like/{userId}")
    public void deleteLikeFromFilm(@PathVariable int id, @PathVariable int userId) {
        filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam Integer count) {
        if (count > 10) {
            count = 10;
        }
        return filmService.getFilmsWithMostLikes(count);
    }
}
