package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

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
@RequiredArgsConstructor
@Slf4j
public class UserDBStorage implements UserStorage {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDBStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public User mapRowToUser(ResultSet resultSet, int rowNum) throws SQLException {
        return User.builder()
                .id(resultSet.getLong("id"))
                .email(resultSet.getString("email"))
                .birthday(resultSet.getDate("birthday").toLocalDate())
                .login(resultSet.getString("login"))
                .name(resultSet.getString("name"))
                .friends(getUserFriends(resultSet.getLong("id"))).build();
    }

    public Long mapRowToFriendId(ResultSet resultSet, int rowNum) throws SQLException {
        return resultSet.getLong("friend_id");
    }

    private Set<Long> getUserFriends(long id) {
        String sqlQuery = "select friend_id from user_friend where user_id = ?";
        List<Long> friendsIdList = jdbcTemplate.query(sqlQuery, this::mapRowToFriendId, id);
        return new HashSet<>(friendsIdList);
    }

    @Override
    public List<User> findAllUsers() {
        String sqlQuery = "select * from users";
        return jdbcTemplate.query(sqlQuery, this::mapRowToUser);
    }

    @Override
    public User findUserById(long id) {
        String sqlQuery = "select * from users where id = ?";
        User user = jdbcTemplate.queryForObject(sqlQuery, this::mapRowToUser, id);
        return user;
    }

    @Override
    public User createUser(User user) {
        String sqlQuery = "insert into users(email, login, name, birthday) values (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"id"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);

        long idFoundUser = keyHolder.getKey().longValue();
        user.setId(idFoundUser);
        log.info("Пользователь с id: " + user.getId() + " создан.");
        return user;
    }

    @Override
    public User updateUser(User user) {
        String sqlQuery = "update users set email = ?, login = ?, name = ?, birthday = ? where id = ?";
        jdbcTemplate.update(sqlQuery, user.getEmail(), user.getLogin(), user.getName(),
                user.getBirthday(), user.getId());
        if (user.getFriends() != null) {
            updateUserFriends(user);
        }
        log.info("Пользователь с id: " + user.getId() + " обновлен.");
        return user;
    }

    public void updateUserFriends(User user) {
        Set<Long> userFriendsUpdated = user.getFriends();
        Set<Long> userFriendsFromDB = new HashSet<>(getUserFriends(user.getId()));

        Set<Long> newUserFriends = userFriendsUpdated.stream()
                .filter(e -> !userFriendsFromDB.contains(e))
                .collect(Collectors.toSet());

        Set<Long> oldFriendsDelete = userFriendsFromDB.stream()
                .filter(e -> !userFriendsUpdated.contains(e))
                .collect(Collectors.toSet());

        String sqlDeleteFriends = "delete from user_friend where user_id = ? and friend_id = ?";
        oldFriendsDelete.stream()
                .map(friendId -> jdbcTemplate.update(sqlDeleteFriends, user.getId(), friendId));

        String sqlAddFriends = "insert into user_friend (user_id, friend_id) values (?, ?)";
        newUserFriends.stream()
                .map(friendId -> jdbcTemplate.update(sqlAddFriends, user.getId(), friendId));
    }

    @Override
    public void deleteUser(User user) {
        String sqlQuery = "delete from users where id = ?";
        if (jdbcTemplate.update(sqlQuery, user.getId()) > 0) {
            log.info("Удален жанр с id: " + user.getId());
        } else {
            log.error("Ошибка при удалении пользователя с id: " + user.getId());
        }
    }

    public boolean addUserToFriends(User user, User friend) {
        String sqlQuery = "insert into user_friend(user_id, friend_id) values (?, ?)";
        return jdbcTemplate.update(sqlQuery, user.getId(), friend.getId()) > 0;
    }

    public boolean deleteFromFriends(User user, User friend) {
        String sqlQuery = "delete from user_friend " +
                "where user_id = ? " +
                "and friend_id = ?;";
        return jdbcTemplate.update(sqlQuery, user.getId(), friend.getId()) > 0;
    }
}
