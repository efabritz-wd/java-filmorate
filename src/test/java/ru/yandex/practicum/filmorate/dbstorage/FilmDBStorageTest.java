package ru.yandex.practicum.filmorate.dbstorage;

import ru.yandex.practicum.filmorate.model.Film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.film.FilmDBStorage;

import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDBStorage.class})
public class FilmDBStorageTest {
    private final FilmDBStorage filmStorage;
    @Test
    public void findAllFilmsTest() {
        assertFalse(filmStorage.findAllFilms().isEmpty());
    }

    @Test
    public void findFilmByIdTest() throws FileNotFoundException {
        Optional<Film> userOptional = Optional.ofNullable(filmStorage.findFilmById(1));

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue("id", 1L)
                );
    }

    @Test
    public void createFilmTest() {
        MPA mpa = MPA.builder()
                .rating("PG2")
                .build();
        filmStorage.createMPA(mpa);
        Film film = Film.builder()
                .name("new film")
                .description("description")
                .duration(120L)
                .releaseDate(LocalDate.parse("2023-01-01"))
                .mpa(mpa)
                .build();
        filmStorage.createFilm(film);
        Film filmFound = filmStorage.findAllFilms().stream()
                .filter(film1 -> film1.getName().equals("new film"))
                .findAny()
                .orElse(null);
        assertNotNull(filmFound);
    }

    @Test
    public void updateFilmTest() throws FileNotFoundException {
        MPA mpa = MPA.builder()
                .rating("PG3")
                .build();
        filmStorage.createMPA(mpa);
        Film film = Film.builder()
                .name("new film 2")
                .description("description")
                .duration(120L)
                .releaseDate(LocalDate.parse("2023-01-01"))
                .mpa(mpa)
                .build();
        filmStorage.createFilm(film);
        film.setDuration(145L);
        Film filmUpdated = filmStorage.updateFilm(film);

        assertEquals(filmStorage.findFilmById(filmUpdated.getId()).getDuration(), 145L);
    }

    @Test
    public void deleteUserTest() {
        MPA mpa = MPA.builder()
                .rating("PG4")
                .build();
        filmStorage.createMPA(mpa);
        Film film = Film.builder()
                .name("new film 4")
                .description("description")
                .duration(120L)
                .releaseDate(LocalDate.parse("2023-01-01"))
                .mpa(mpa)
                .build();
        Film filmCreated = filmStorage.createFilm(film);
        int userListSize = filmStorage.findAllFilms().size();
        filmStorage.deleteFilm(filmCreated);
        assertEquals(userListSize, filmStorage.findAllFilms().size() + 1);
    }
}
