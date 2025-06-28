package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Primary
@Repository
//@RequiredArgsConstructor
@Slf4j
public class FilmDBStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmDBStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Film mapRowToFilm(ResultSet resultSet, int rowNum) throws SQLException {
        return Film.builder()
                .id(resultSet.getLong("id"))
                .name(resultSet.getString("name"))
                .releaseDate(resultSet.getDate("releaseDate").toLocalDate())
                .description(resultSet.getString("description"))
                .duration(resultSet.getLong("duration"))
                .mpa(findMPAById(resultSet.getLong("rating")))
                .genres(findGenresByFilmId(resultSet.getLong("id"))).build();
    }

    public Genre mapRowToGenre(ResultSet resultSet, int rowNum) throws SQLException {
        return Genre.builder()
                .id(resultSet.getLong("id"))
                .name(resultSet.getString("name")).build();
    }

    public MPA mapRowToMPA(ResultSet resultSet, int rowNum) throws SQLException {
        return MPA.builder()
                .id(resultSet.getLong("id"))
                .rating(resultSet.getString("rating")).build();
    }

    /* MPA */
    public List<MPA> findAllMPA() {
        String sqlQuery = "select * from mpa";
        return jdbcTemplate.query(sqlQuery, this::mapRowToMPA);
    }

    public MPA findMPAById(long id) {
        String sqlQuery = "select * from mpa where id = ?";
        return jdbcTemplate.queryForObject(sqlQuery, this::mapRowToMPA, id);
    }

    public MPA createMPA(MPA mpa) {
        String sqlQuery = "insert into mpa(rating) " +
                "values (?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"id"});
            stmt.setString(1, mpa.getRating());
            return stmt;
        }, keyHolder);
        long idOfCreatedMPA = keyHolder.getKey().longValue();
        mpa.setId(idOfCreatedMPA);
        log.info("Рейтинг с id: " + mpa.getId() + " создан.");

        return mpa;
    }

    public MPA updateMPA(MPA mpa) {
        String sqlQuery = "update mpa set rating = ? where id = ?";
        jdbcTemplate.update(sqlQuery,
                mpa.getRating());

        log.info("MPA с id: " + mpa.getId() + " обновлен.");
        return mpa;
    }

    public void deleteMPA(MPA mpa) {
        String sqlQuery = "delete from mpa where id = ?";
        if (jdbcTemplate.update(sqlQuery, mpa.getId()) > 0) {
            log.info("Удален фильма с id: " + mpa.getId());
        } else {
            log.error("Ошибка при удалении фильма с id: " + mpa.getId());
        }
    }

    /* Film */
    @Override
    public List<Film> findAllFilms() {
        String sqlQuery = "select * from film;";
        log.info("Выборка фильмов");
        return jdbcTemplate.query(sqlQuery, this::mapRowToFilm);
    }

    @Override
    public Film findFilmById(long id) {
        String sqlQuery = "select * from film where id = ?";
        return jdbcTemplate.queryForObject(sqlQuery, this::mapRowToFilm, id);
    }

    @Override
    public Film createFilm(Film film) {
        String sqlQuery = "insert into film(name, description, releaseDate, duration, rating) " +
                "values (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setLong(4, film.getDuration());
            stmt.setLong(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);
        long idOfCreatedFilm = keyHolder.getKey().longValue();
        film.setId(idOfCreatedFilm);

        log.info("Фильс с id: " + film.getId() + " создан.");

        if (film.getGenres() != null) {
            String sqlInsertFilmGenres = "insert into film_genre(film_id, genre_id) values (?, ?)";
            film.getGenres().stream()
                    .map(genre -> jdbcTemplate.update(sqlInsertFilmGenres, film.getId(), genre.getId()));
        }
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        String sqlQuery = "update film set name = ?, description = ?, releaseDate = ?, duration = ?, rating = ?" +
                "where id = ?";
        jdbcTemplate.update(sqlQuery,
                 film.getName(),
                 film.getDescription(),
                 film.getReleaseDate(),
                 film.getDuration(),
                 film.getMpa().getId(),
                 film.getId());

        log.info("Фильс с id: " + film.getId() + " обновлен.");

        if (film.getGenres() != null) {
            updateFilmGenres(film);
        }
        if(film.getLikes() != null) {
            updateFilmLikes(film);
        }
        log.info("Обновление жанров для фильма с id: " + film.getId());

        return film;
    }

    public List<Long> findUserIdFromFilmLiked(Film film) {
        String sqlQuery = "select user_id from film_like where film_id = ?";
        return jdbcTemplate.query(sqlQuery, (resultSet, rowNum) -> resultSet.getLong("user_id"), film.getId());
    }

    private void updateFilmLikes(Film film) {
        Set<Long> userIdsLikesUpdated = film.getLikes();
        Set<Long> userIdsLikesFromDB = new HashSet<>(findUserIdFromFilmLiked(film));

        Set<Long> newLikesOfFilm = userIdsLikesUpdated.stream()
                .filter(e -> !userIdsLikesFromDB.contains(e))
                .collect(Collectors.toSet());

        Set<Long> oldLikesOfFilmDelete = userIdsLikesFromDB.stream()
                .filter(e -> !userIdsLikesUpdated.contains(e))
                .collect(Collectors.toSet());

        String sqlDeleteLikes = "delete from film_like where film_id = ? and user_id = ?";
        oldLikesOfFilmDelete.stream()
                .map(userId -> jdbcTemplate.update(sqlDeleteLikes, film.getId(), userId));

        String sqlAddLikes = "insert into film_like (film_id, user_id) values (?, ?)";
        newLikesOfFilm.stream()
                .map(userId -> jdbcTemplate.update(sqlAddLikes, film.getId(), userId));

    }

    @Override
    public void deleteFilm(Film film) {
        String sqlQuery = "delete from film where id = ?";
        jdbcTemplate.update(sqlQuery, film.getId());
    }


    /* Genre */
    public List<Genre> findAllGenres() {
        String sqlQuery = "select * from genre";
        return jdbcTemplate.query(sqlQuery, this::mapRowToGenre);
    }

    public Genre findGenreById(long id) {
        String sqlQuery = "select * from genre where id = ?";
        return jdbcTemplate.queryForObject(sqlQuery, this::mapRowToGenre, id);
    }

    private Film updateFilmGenres(Film film) {
        Set<Genre> genres = film.getGenres().stream()
                .map(genre -> findGenreById(genre.getId()))
                .collect(Collectors.toSet());
        film.setGenres(genres);
        for (Genre genre : film.getGenres()) {
            createFilmGenre(film.getId(), genre.getId());
        }
        return film;
    }

    public Genre createGenre(Genre genre) {
        String sqlQuery = "insert into genre(name) " +
                "values (?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"id"});
            stmt.setString(1, genre.getName());
            return stmt;
        }, keyHolder);
        long idOfCreatedGenre = keyHolder.getKey().longValue();
        genre.setId(idOfCreatedGenre);
        log.info("Жанр с id: " + genre.getId() + " создан.");

        return genre;
    }

    public Genre updateGenre(Genre genre) {
        String sqlQuery = "update genre set name = ? where id = ?";
        jdbcTemplate.update(sqlQuery,
                genre.getName());

        log.info("Жанр с id: " + genre.getId() + " обновлен.");
        return genre;
    }

    public void deleteGenre(Genre genre) {
        String sqlQuery = "delete from genre where id = ?";
        if (jdbcTemplate.update(sqlQuery, genre.getId()) > 0) {
            log.info("Удален жанр с id: " + genre.getId());
        } else {
            log.error("Ошибка при удалении жанра с id: " + genre.getId());
        }
    }

    private void createFilmGenre(Long id, Long genreId) {
        String sqlCreateFilmGenreIf = "insert into film_genre(film_id, genre_id) " +
                "select ?, ? from dual where not exists " +
                "(select 1 from film_genre where film_id = ?, genre_id = ?);";

        jdbcTemplate.update(sqlCreateFilmGenreIf, id, genreId, id, genreId);
    }

    private Set<Genre> findGenresByFilmId(Long id) {
        String sqlQuery = "select * from film_genre as fgs " +
                "left join genre as g on fgs.genre_id = genre_id where fgs.film_id = ?";
        List<Genre> genresOfFilmList = jdbcTemplate.query(sqlQuery, this::mapRowToGenre, id);
        return new HashSet<>(genresOfFilmList);
    }

    private void deleteFilmGenres(Long id) {
        String sqlQuery = "delete from film_genre where film_id = ?";
        jdbcTemplate.update(sqlQuery, id);
    }
}
