package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;


class FilmControllerTest {
    private InMemoryFilmStorage filmStorage;

    @Test
    void validateFilm() {
        MPA mpa = MPA.builder()
                .id(1L)
                .name("pg test")
                .build();
        Film film = Film.builder()
                .name("new film 2")
                .description("description")
                .duration(120L)
                .releaseDate(LocalDate.parse("2023-01-01"))
                .mpa(mpa)
                .build();

        film.setName("");
        film.setDuration(Duration.ofHours(2).toMinutes());
        film.setReleaseDate(LocalDate.of(1999, 12, 7));
        film.setDescription("Description");
        assertFalse(filmStorage.validateFilm(film, true));


        film.setName("film");
        film.setDuration(120L);
        film.setReleaseDate(filmStorage.boundaryReleaseDate.minusDays(1));
        film.setDescription("Description");
        assertFalse(filmStorage.validateFilm(film, true));


        film.setName("film");
        film.setDuration(120L);
        film.setReleaseDate(LocalDate.of(1999, 12, 7));
        film.setDescription("czgbhucejkcubxetkjrihjohcwfdipsxahjpbwwyjcouljmulbyjpyizkfeoozkstpou" +
                "qutboxvvdjnosvcjhiuwojtukpwbzxdlcrdivotmycmlofjzonunkwxzdjqzvcbaldpwlmkwroacnxiqvoekbsshyzcsifsrqxla" +
                "zhhtsahereqvnpavvqrcpxexxgjtysyfgdrysmizoczrqgdzyyuarpzszs" +
                "frznrmasejzelbpygnkkpihnjprdcmotfbqcafgmroqhfcjzumdufisnoalxbgbnnucmybdmzrjgkzipaunnoagyzorpkozburoa" +
                "icwbeoqjygwcxfypskdcebojkhhjezsygpnjmsmdfgicumowvaggkf");
        assertFalse(filmStorage.validateFilm(film, true));

        film.setName("film");
        film.setDuration(-30L);
        film.setReleaseDate(LocalDate.of(1999, 12, 7));
        film.setDescription("Description");
        assertFalse(filmStorage.validateFilm(film, true));

        film.setName("Film");
        film.setDuration(120L);
        film.setReleaseDate(LocalDate.of(1999, 12, 7));
        film.setDescription("Description");
        assertTrue(filmStorage.validateFilm(film, true));
    }
}