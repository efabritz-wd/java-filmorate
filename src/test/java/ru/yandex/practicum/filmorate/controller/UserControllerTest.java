package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    @Test
    void validateUser() {
        User userEmpty = new User();
        assertFalse(UserController.validateUser(userEmpty, true));

        User userBlankEmail = new User();
        userBlankEmail.setName("Vasja");
        userBlankEmail.setEmail("");
        userBlankEmail.setLogin("vasja");
        userBlankEmail.setBirthday(LocalDate.now().minusYears(20));
        assertFalse(UserController.validateUser(userBlankEmail, true));

        User userEmailWithoutSymbol = new User();
        userEmailWithoutSymbol.setName("Vasja");
        userEmailWithoutSymbol.setEmail("vasjagmail.com");
        userEmailWithoutSymbol.setLogin("vasja");
        userEmailWithoutSymbol.setBirthday(LocalDate.now().minusYears(20));
        assertFalse(UserController.validateUser(userEmailWithoutSymbol, true));

        User userLoginBlank = new User();
        userLoginBlank.setName("Vasja");
        userLoginBlank.setEmail("vasja@gmail.com");
        userLoginBlank.setLogin("");
        userLoginBlank.setBirthday(LocalDate.now().minusYears(20));
        assertFalse(UserController.validateUser(userLoginBlank, true));

        User userLoginContainsEmptySym = new User();
        userLoginContainsEmptySym.setName("Vasja");
        userLoginContainsEmptySym.setEmail("vasja@gmail.com");
        userLoginContainsEmptySym.setLogin("vas ja");
        userLoginContainsEmptySym.setBirthday(LocalDate.now().minusYears(20));
        assertFalse(UserController.validateUser(userLoginContainsEmptySym, true));

        User userNameIsBlank = new User();
        userNameIsBlank.setName("");
        userNameIsBlank.setEmail("vasja@gmail.com");
        userNameIsBlank.setLogin("vasja");
        userNameIsBlank.setBirthday(LocalDate.now().minusYears(20));
        assertTrue(UserController.validateUser(userNameIsBlank, true));

        User userGetBirthdayAfterNow = new User();
        userGetBirthdayAfterNow.setName("Vasja");
        userGetBirthdayAfterNow.setEmail("vasja@gmail.com");
        userGetBirthdayAfterNow.setLogin("vasja");
        userGetBirthdayAfterNow.setBirthday(LocalDate.now().plusDays(1));
        assertFalse(UserController.validateUser(userGetBirthdayAfterNow, true));

        User userCorrect = new User();
        userCorrect.setName("Vasja");
        userCorrect.setEmail("vasja@gmail.com");
        userCorrect.setLogin("vasja");
        userCorrect.setBirthday(LocalDate.now().minusYears(20));
        assertTrue(UserController.validateUser(userCorrect, true));
    }
}