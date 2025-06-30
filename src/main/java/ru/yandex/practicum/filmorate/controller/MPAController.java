package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.service.film.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/mpa")
public class MPAController {
    private final FilmService filmService;

    @Autowired
    public MPAController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public List<MPA> findAll() {
        return filmService.findAllMPA();
    }

    @GetMapping(value = "/{mpaId}")
    public MPA findMPAById(@PathVariable int mpaId) {
        if (filmService.findMPAById(mpaId).isPresent()) {
            return filmService.findMPAById(mpaId).get();
        }
        throw new NotFoundException("MPA not found", MPAController.class.getName());
    }
}
