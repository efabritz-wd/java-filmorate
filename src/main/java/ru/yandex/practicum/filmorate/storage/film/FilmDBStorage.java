package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;

import java.io.FileNotFoundException;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage.validateFilm;

@Primary
@Repository
@Slf4j
public class FilmDBStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmDBStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Film mapRowToFilm(ResultSet resultSet, int rowNum) throws SQLException {
        MPA.MPABuilder mpa;
        MPA objMPA = null;
        Optional<Long> mpaId = Optional.ofNullable(resultSet.getLong("rating"));
        Optional<String> mpaName = Optional.ofNullable(resultSet.getString("mpa_name"));
        if (mpaId.isPresent()) {
            mpa = MPA.builder().id(mpaId.get());
            if (mpaName.isPresent()) {
                mpa.name(mpaName.get());
            }
            objMPA = mpa.build();
        }

        Film.FilmBuilder filmBuilder = Film.builder()
                .id(resultSet.getLong("id"))
                .name(resultSet.getString("name"))
                .releaseDate(resultSet.getDate("releaseDate").toLocalDate())
                .description(resultSet.getString("description"))
                .duration(resultSet.getLong("duration"))
                .likes(findUserLikesByFilmId(resultSet.getLong("id")))
                .genres(findGenresByFilmId(resultSet.getLong("id")))
                .mpa(objMPA);
        return filmBuilder.build();
    }

    public Genre mapRowToGenre(ResultSet resultSet, int rowNum) throws SQLException {
        return Genre.builder()
                .id(resultSet.getLong("id"))
                .name(resultSet.getString("name")).build();
    }

    public MPA mapRowToMPA(ResultSet resultSet, int rowNum) throws SQLException {
        return MPA.builder()
                .id(resultSet.getLong("id"))
                .name(resultSet.getString("name")).build();
    }

    /* MPA */
    public List<MPA> findAllMPAs() {
        String sqlQuery = "select * from mpa";
        return jdbcTemplate.query(sqlQuery, this::mapRowToMPA);
    }

    public Optional<MPA> findMPAById(long id) {
        String sqlQuery = "select * from mpa where id = ?";
        try {
            return Optional.of(jdbcTemplate.queryForObject(sqlQuery, this::mapRowToMPA, id));
        } catch (EmptyResultDataAccessException e) {
            log.error("MPA not found for ID: " + id);
            return Optional.empty();
        }

    }

    public MPA createMPA(MPA mpa) {
        String sqlQuery = "insert into mpa(name) " +
                "values (?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"id"});
            stmt.setString(1, mpa.getName());
            return stmt;
        }, keyHolder);
        long idOfCreatedMPA = keyHolder.getKey().longValue();
        mpa.setId(idOfCreatedMPA);
        log.info("Рейтинг с id: " + mpa.getId() + " создан.");

        return mpa;
    }

    public MPA updateMPA(MPA mpa) {
        String sqlQuery = "update mpa set name = ? where id = ?";
        jdbcTemplate.update(sqlQuery,
                mpa.getName());

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

    public boolean checkIfMPAIdExists(long id) {
        return findMPAById(id).isPresent();
    }

    /* Film */
    @Override
    public List<Film> findAllFilms() {
        String sqlQuery = "select f.*, m.*, m.name AS mpa_name from film f join mpa as m on f.rating = m.id";
        log.info("Выборка фильмов");
        List<Film> res = jdbcTemplate.query(sqlQuery, this::mapRowToFilm);
        return res;
    }

    @Override
    public Film findFilmById(long id) throws FileNotFoundException {
        String sqlQuery = "select f.*, m.*, m.name AS mpa_name from film f join mpa m on f.rating = m.id  where f.id = ?";
        String sqlCheckIfExists = "SELECT COUNT(*) FROM film WHERE id = ?";

        int count = jdbcTemplate.queryForObject(sqlCheckIfExists, new Object[]{id}, Integer.class);
        if (count == 0) {
            throw new NotFoundException("Фильм с id: " + id + " не найден.", FilmStorage.class.getName());
        }

        return jdbcTemplate.queryForObject(sqlQuery, this::mapRowToFilm, id);
    }

    @Override
    public Film createFilm(Film film) {
        if (!validateFilm(film, true)) {
            throw new ValidationException("Ошибка валидации фильма при создании", Film.class.getName());
        }
        String sqlQuery = "insert into film(name, description, releaseDate, duration, rating) " +
                "values (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setLong(4, film.getDuration());

            if (film.getMpa() != null) {
                if (checkIfMPAIdExists(film.getMpa().getId())) {
                    stmt.setLong(5, film.getMpa().getId());
                } else {
                    throw new NotFoundException("MPA с id: " + film.getMpa().getId() + " не найдено",
                            FilmDBStorage.class.getName());
                }
            } else {
                stmt.setNull(5, java.sql.Types.BIGINT);
            }

            return stmt;
        }, keyHolder);
        long idOfCreatedFilm = keyHolder.getKey().longValue();
        film.setId(idOfCreatedFilm);

        log.info("Фильс с id: " + film.getId() + " создан.");

        if (film.getGenres() != null) {
            List<Genre> genreList = new ArrayList<>(film.getGenres());

            jdbcTemplate.batchUpdate("INSERT INTO film_genre(film_id, genre_id) VALUES (?, ?)", new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                    Long genreId = genreList.get(i).getId();

                    if (findGenreById(genreId) == null) {
                        throw new NotFoundException("Жарна с id: " + genreId + " не существует.",
                                FilmDBStorage.class.getName());
                    }

                    preparedStatement.setString(1, String.valueOf(film.getId()));
                    preparedStatement.setString(2, String.valueOf(genreId));
                }

                @Override
                public int getBatchSize() {
                    return film.getGenres().size();
                }
            });
        }

        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (film.getId() == null) {
            log.error("Обновление фильма: не указан id");
            throw new NotFoundException("Id должен быть указан", Film.class.getName());
        }
        String sqlCheckIfExists = "SELECT COUNT(*) FROM film WHERE id = ?";
        int count = jdbcTemplate.queryForObject(sqlCheckIfExists, new Object[]{film.getId()}, Integer.class);
        if (count == 0) {
            log.error("Обновление фильма: Фильм не найден");
            throw new NotFoundException("Фильма с id " + film.getId() + " не существует", Film.class.getName());
        }
        if (!validateFilm(film, true)) {
            throw new ValidationException("Ошибка валидации фильма при создании", Film.class.getName());
        }
        String sqlQuery = "update film set name = ?, description = ?, releaseDate = ?, duration = ?, rating = ?" +
                "where id = ?";

        Long mpaId = film.getMpa() != null ? film.getMpa().getId() : null;
        jdbcTemplate.update(sqlQuery,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                mpaId,
                film.getId());

        log.info("Фильс с id: " + film.getId() + " обновлен.");

        if (film.getGenres() != null) {
            updateFilmGenres(film);
        }
        if (film.getLikes() != null) {
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
        List<Long> likesList = new ArrayList<>(userIdsLikesUpdated);

        String sqlDeleteLikes = "delete from film_like where film_id = ?";
        jdbcTemplate.update(sqlDeleteLikes, film.getId());

        jdbcTemplate.batchUpdate("insert into film_like(film_id, user_id) values (?, ?)", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                Long userId = likesList.get(i);

                int count = jdbcTemplate.queryForObject("select COUNT(*) from users where id=?", new Object[]{userId},
                        Integer.class);
                if (count == 0) {
                    throw new NotFoundException("Пользователь с ID: " + userId + " не найден", FilmDBStorage.class.getName());
                }

                preparedStatement.setString(1, String.valueOf(film.getId()));
                preparedStatement.setString(2, String.valueOf(userId));
            }

            @Override
            public int getBatchSize() {
                return likesList.size();
            }
        });

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
        String sqlCheckIfExists = "select COUNT(*) from genre where id = ?";

        int count = jdbcTemplate.queryForObject(sqlCheckIfExists, new Object[]{id}, Integer.class);
        if (count == 0) {
            throw new NotFoundException("Жанр с ID: " + id + " не найден", FilmDBStorage.class.getName());
        }
        return jdbcTemplate.queryForObject(sqlQuery, this::mapRowToGenre, id);
    }

    private Film updateFilmGenres(Film film) {
        Set<Genre> genres = film.getGenres().stream()
                .map(genre -> findGenreById(genre.getId()))
                .collect(Collectors.toSet());
        film.setGenres(genres);
        updateFilmGenreDB(film);
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
            log.info("Жанр с id: " + genre.getId() + " удален.");
        } else {
            log.error("Жанра с id: " + genre.getId() + " не может быть удален");
        }
    }

    private void createFilmGenre(Long id, Long genreId) {
        String sqlCreateFilmGenreIf = "insert into film_genre (film_id, genre_id) " +
                "select ?, ? where NOT exists " +
                "(select 1 from film_genre where film_id = ? and genre_id = ?)";


        jdbcTemplate.update(sqlCreateFilmGenreIf, id, genreId, id, genreId);
    }

    public void updateFilmGenreDB(Film film) {
        Set<Genre> userGenresAfterUpdate = film.getGenres();
        List<Genre> userGenresList = new ArrayList<>(userGenresAfterUpdate);

        String sqlDeleteGenres = "delete from film_genre where film_id = ?";
        jdbcTemplate.update(sqlDeleteGenres, film.getId());


        jdbcTemplate.batchUpdate("INSERT INTO film_genre(film_id, genre_id) VALUES (?, ?)", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                Long genreId = userGenresList.get(i).getId();

                if (findGenreById(genreId) == null) {
                    throw new NotFoundException("Жарна с id: " + genreId + " не существует.",
                            FilmDBStorage.class.getName());
                }

                preparedStatement.setString(1, String.valueOf(film.getId()));
                preparedStatement.setString(2, String.valueOf(genreId));
            }

            @Override
            public int getBatchSize() {
                return film.getGenres().size();
            }
        });

       /*
        Set<Genre> userGenresBeforeUpdate = findGenresByFilmId(film.getId());

        Set<Genre> newGenresOfFilm = userGenresAfterUpdate.stream()
                .filter(e -> !userGenresBeforeUpdate.contains(e))
                .collect(Collectors.toSet());

        Set<Genre> oldGenresOfFilmDelete = userGenresBeforeUpdate.stream()
                .filter(e -> !userGenresAfterUpdate.contains(e))
                .collect(Collectors.toSet());

        String sqlDeleteGenres = "delete from film_genre where film_id = ? and genre_id = ?";
        for (Genre genreFound : oldGenresOfFilmDelete) {
            jdbcTemplate.update(sqlDeleteGenres, film.getId(), genreFound.getId());
        }

        String sqlAddGenres = "insert into film_genre (film_id, genre_id) values (?, ?)";
        for (Genre userId : newGenresOfFilm) {
            jdbcTemplate.update(sqlAddGenres, film.getId(), userId);
        }*/
    }

    private Set<Genre> findGenresByFilmId(Long id) {
        String sqlQuery = "select * from genre as g1 where g1.id in " +
                "(select f1.genre_id from film_genre as f1 where f1.film_id = ?)";

        List<Genre> genresOfFilmList = jdbcTemplate.query(sqlQuery, this::mapRowToGenre, id);
        return new HashSet<>(genresOfFilmList);
    }

    private void deleteFilmGenres(Long id) {
        String sqlQuery = "delete from film_genre where film_id = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    /* Likes */
    private Set<Long> findUserLikesByFilmId(Long id) {
        String sqlQuery = "select user_id from film_like where film_id = ?";
        List<Long> userLikesList = jdbcTemplate.query(sqlQuery, new Object[]{id}, (rs, rowNum) -> rs.getLong("user_id"));
        return new HashSet<>(userLikesList);
    }
}
