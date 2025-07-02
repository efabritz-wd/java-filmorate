package ru.yandex.practicum.filmorate.dbstorage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDBStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDBStorage.class})
class UserDBStorageTest {
    private final UserDBStorage userStorage;

    @Test
    public void testFindAllUsers() {
        List<User> users = userStorage.findAllUsers();
        assertFalse(users.isEmpty());
    }

    @Test
    public void testFindUserById() {
        Optional<User> userOptional = Optional.ofNullable(userStorage.findUserById(1));

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 1L)
                );
    }

    @Test
    public void createUserTest() {
        User user = User.builder()
                .login("user1")
                .email("user1l@mail.ru")
                .name("user1log")
                .birthday(LocalDate.parse("2000-01-01"))
                .build();
        userStorage.createUser(user);
        User userFound = userStorage.findAllUsers().stream()
                .filter(user1 -> user1.getEmail().equals("user1l@mail.ru"))
                .findAny()
                .orElse(null);
        assertNotNull(userFound);
    }

    @Test
    public void updateUserTest() {
        User user = User.builder()
                .login("user1")
                .email("user1l@mail.ru")
                .name("user1log")
                .birthday(LocalDate.parse("2000-01-01"))
                .build();
        userStorage.createUser(user);
        user.setName("userUpdated");
        User userUpdated = userStorage.updateUser(user);

        assertEquals(userStorage.findUserById(userUpdated.getId()).getName(), "userUpdated");
    }

    @Test
    public void deleteUserTest() {
        User user = User.builder()
                .login("user1")
                .email("user1l@mail.ru")
                .name("user1log")
                .birthday(LocalDate.parse("2000-01-01"))
                .build();
        User userCreated = userStorage.createUser(user);
        int userListSize = userStorage.findAllUsers().size();
        userStorage.deleteUser(userCreated);
        assertEquals(userListSize, userStorage.findAllUsers().size() + 1);
    }
}