package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

    @Test
    void validateFilm() {
        Film filmEmpty = new Film();
        assertFalse(FilmController.validateFilm(filmEmpty, true));

        Film filmBlankName = new Film();
        filmBlankName.setName("");
        filmBlankName.setDuration(Duration.ofHours(2).toMinutes());
        filmBlankName.setReleaseDate(LocalDate.of(1999, 12, 7));
        filmBlankName.setDescription("Description");
        assertFalse(FilmController.validateFilm(filmBlankName, true));

        Film filmReleaseDateOld = new Film();
        filmReleaseDateOld.setName("film");
        filmReleaseDateOld.setDuration(120L);
        filmReleaseDateOld.setReleaseDate(FilmController.boundaryReleaseDate.minusDays(1));
        filmReleaseDateOld.setDescription("Description");
        assertFalse(FilmController.validateFilm(filmReleaseDateOld, true));

        Film filmMore200Description = new Film();
        filmMore200Description.setName("film");
        filmMore200Description.setDuration(120L);
        filmMore200Description.setReleaseDate(LocalDate.of(1999, 12, 7));
        filmMore200Description.setDescription("czgbhucejkcubxetkjrihjohcwfdipsxahjpbwwyjcouljmulbyjpyizkfeoozkstpou" +
                "qutboxvvdjnosvcjhiuwojtukpwbzxdlcrdivotmycmlofjzonunkwxzdjqzvcbaldpwlmkwroacnxiqvoekbsshyzcsifsrqxla" +
                "zhhtsahereqvnpavvqrcpxexxgjtysyfgdrysmizoczrqgdzyyuarpzszs" +
                "frznrmasejzelbpygnkkpihnjprdcmotfbqcafgmroqhfcjzumdufisnoalxbgbnnucmybdmzrjgkzipaunnoagyzorpkozburoa" +
                "icwbeoqjygwcxfypskdcebojkhhjezsygpnjmsmdfgicumowvaggkf");
        assertFalse(FilmController.validateFilm(filmMore200Description, true));

        Film filmNegativDuration = new Film();
        filmNegativDuration.setName("film");
        filmNegativDuration.setDuration(-30L);
        filmNegativDuration.setReleaseDate(LocalDate.of(1999, 12, 7));
        filmNegativDuration.setDescription("Description");
        assertFalse(FilmController.validateFilm(filmNegativDuration, true));

        Film filmCorrect = new Film();
        filmCorrect.setName("Film");
        filmCorrect.setDuration(120L);
        filmCorrect.setReleaseDate(LocalDate.of(1999, 12, 7));
        filmCorrect.setDescription("Description");
        assertTrue(FilmController.validateFilm(filmCorrect, true));
    }
}