package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.film.FilmService;

import java.io.FileNotFoundException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping()
    public List<Film> findAll() {
        return filmService.findAllFilms();
    }

    @GetMapping(value = "/{filmId}")
    public Film findFilmById(@PathVariable int filmId) throws FileNotFoundException {
        return filmService.findFilm(filmId);
    }

    @PostMapping()
    public Film createFilm(@Valid @RequestBody Film film) {
        return filmService.createFilm(film);
    }

    @PutMapping()
    public Film updateFilm(@Valid @RequestBody Film newFilm) {
        return filmService.updateFilm(newFilm);
    }

    @PutMapping(value = "/{id}/like/{userId}")
    public void setLikeToFilm(@PathVariable int id, @PathVariable int userId) throws FileNotFoundException {
        filmService.addLikeToFilm(id, userId);
    }

    @DeleteMapping(value = "/{id}/like/{userId}")
    public void deleteLikeFromFilm(@PathVariable int id, @PathVariable int userId) throws FileNotFoundException {
        filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam Integer count) {
        System.out.println("get popular called");
        if (count > 10) {
            count = 10;
        }
        return filmService.getFilmsWithMostLikes(count);
    }
}
