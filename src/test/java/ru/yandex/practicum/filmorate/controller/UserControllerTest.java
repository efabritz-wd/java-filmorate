package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private InMemoryUserStorage userStorage;

    @Test
    void validateUser() {
        User user = User.builder()
                .name("name")
                .login("log")
                .email("test@mail.ru")
                .birthday(LocalDate.parse("2023-01-01"))
                .build();

        user.setName("Vasja");
        user.setEmail("");
        user.setLogin("vasja");
        user.setBirthday(LocalDate.now().minusYears(20));
        assertFalse(userStorage.validateUser(user, true));

        user.setName("Vasja");
        user.setEmail("vasjagmail.com");
        user.setLogin("vasja");
        user.setBirthday(LocalDate.now().minusYears(20));
        assertFalse(userStorage.validateUser(user, true));

        user.setName("Vasja");
        user.setEmail("vasja@gmail.com");
        user.setLogin("");
        user.setBirthday(LocalDate.now().minusYears(20));
        assertFalse(userStorage.validateUser(user, true));

        user.setName("Vasja");
        user.setEmail("vasja@gmail.com");
        user.setLogin("vas ja");
        user.setBirthday(LocalDate.now().minusYears(20));
        assertFalse(userStorage.validateUser(user, true));

        user.setName("");
        user.setEmail("vasja@gmail.com");
        user.setLogin("vasja");
        user.setBirthday(LocalDate.now().minusYears(20));
        assertTrue(userStorage.validateUser(user, true));

        user.setName("Vasja");
        user.setEmail("vasja@gmail.com");
        user.setLogin("vasja");
        user.setBirthday(LocalDate.now().plusDays(1));
        assertFalse(userStorage.validateUser(user, true));

        user.setName("Vasja");
        user.setEmail("vasja@gmail.com");
        user.setLogin("vasja");
        user.setBirthday(LocalDate.now().minusYears(20));
        assertTrue(userStorage.validateUser(user, true));
    }
}