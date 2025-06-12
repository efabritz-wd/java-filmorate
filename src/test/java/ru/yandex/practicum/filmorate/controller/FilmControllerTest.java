package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

    @Test
    void validateFilm() {
        Film filmEmpty = new Film();
        InMemoryFilmStorage filmStorage = new InMemoryFilmStorage();
        assertFalse(filmStorage.validateFilm(filmEmpty, true));

        Film filmBlankName = new Film();
        filmBlankName.setName("");
        filmBlankName.setDuration(Duration.ofHours(2).toMinutes());
        filmBlankName.setReleaseDate(LocalDate.of(1999, 12, 7));
        filmBlankName.setDescription("Description");
        assertFalse(filmStorage.validateFilm(filmBlankName, true));

        Film filmReleaseDateOld = new Film();
        filmReleaseDateOld.setName("film");
        filmReleaseDateOld.setDuration(120L);
        filmReleaseDateOld.setReleaseDate(filmStorage.boundaryReleaseDate.minusDays(1));
        filmReleaseDateOld.setDescription("Description");
        assertFalse(filmStorage.validateFilm(filmReleaseDateOld, true));

        Film filmMore200Description = new Film();
        filmMore200Description.setName("film");
        filmMore200Description.setDuration(120L);
        filmMore200Description.setReleaseDate(LocalDate.of(1999, 12, 7));
        filmMore200Description.setDescription("czgbhucejkcubxetkjrihjohcwfdipsxahjpbwwyjcouljmulbyjpyizkfeoozkstpou" +
                "qutboxvvdjnosvcjhiuwojtukpwbzxdlcrdivotmycmlofjzonunkwxzdjqzvcbaldpwlmkwroacnxiqvoekbsshyzcsifsrqxla" +
                "zhhtsahereqvnpavvqrcpxexxgjtysyfgdrysmizoczrqgdzyyuarpzszs" +
                "frznrmasejzelbpygnkkpihnjprdcmotfbqcafgmroqhfcjzumdufisnoalxbgbnnucmybdmzrjgkzipaunnoagyzorpkozburoa" +
                "icwbeoqjygwcxfypskdcebojkhhjezsygpnjmsmdfgicumowvaggkf");
        assertFalse(filmStorage.validateFilm(filmMore200Description, true));

        Film filmNegativDuration = new Film();
        filmNegativDuration.setName("film");
        filmNegativDuration.setDuration(-30L);
        filmNegativDuration.setReleaseDate(LocalDate.of(1999, 12, 7));
        filmNegativDuration.setDescription("Description");
        assertFalse(filmStorage.validateFilm(filmNegativDuration, true));

        Film filmCorrect = new Film();
        filmCorrect.setName("Film");
        filmCorrect.setDuration(120L);
        filmCorrect.setReleaseDate(LocalDate.of(1999, 12, 7));
        filmCorrect.setDescription("Description");
        assertTrue(filmStorage.validateFilm(filmCorrect, true));
    }
}