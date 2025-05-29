package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.FilmValidationException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    public static boolean validateUser(User user, boolean creation) {
        if (creation && (user.getEmail() == null || user.getLogin() == null)) {
            log.error("Ошибка валидации: email или пароль null");
            return false;
        }

        if (!creation && (user.getEmail() == null || user.getLogin() == null || user.getName() == null || user.getBirthday() == null)) {
            log.error("Ошибка валидации: email или пароль null");
            return false;
        }

        if (user.getEmail() != null && (user.getEmail().isBlank() || !user.getEmail().contains("@"))) {
            log.error("Ошибка валидации: некорректный email");
            return false;
        }
        if (user.getLogin() != null && (user.getLogin().isBlank() || user.getLogin().contains(" "))) {
            log.error("Ошибка валидации: login пустой или с пробелом");
            return false;
        }

        if (creation && (user.getName() == null || (user.getName().isBlank()))) {
            log.debug("Валидация: имя пользователя отсутствует, будет использован login");
            String login = user.getLogin();
            user.setName(login);
        }

        if (!creation && user.getName() != null && (user.getName().isBlank())) {
            log.debug("Валидация: имя пользователя отсутствует, будет использован login");
            String login = user.getLogin();
            user.setName(login);
        }

        if (user.getBirthday() != null && (user.getBirthday().isAfter(LocalDate.now()))) {
            log.error("Ошибка валидации: некорректный день рождения");
            return false;
        }
        return true;
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        if (!validateUser(user, true)) {
            throw new FilmValidationException("Фильм не прошел валидацию при добавлении");
        }
        log.info("Новый пользователь прошел валидацию. Создание полей пользователя");
        user.setId(getNextId());
        user.setName(user.getName());
        user.setEmail(user.getEmail());
        user.setLogin(user.getLogin());
        user.setBirthday(user.getBirthday());

        users.put(user.getId(), user);
        log.info("Новый пользователь добавлен");
        return user;
    }

    private int getNextId() {
        int currentMaxId = users.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        if (newUser.getId() == null) {
            log.error("Обновление пользователя: не указан id");
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        if (!validateUser(newUser, false)) {
            throw new UserValidationException("Пользователь не прошел валидацию при обновлении");
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());

            if (newUser.getName() != null) {
                log.info("Обновление имени пользователя");
                oldUser.setName(newUser.getName());
            }
            if (newUser.getEmail() != null) {
                log.info("Обновление мэйла пользователя");
                oldUser.setEmail(newUser.getEmail());
            }
            if (newUser.getLogin() != null) {
                log.info("Обновление логина пользователя");
                oldUser.setLogin(newUser.getLogin());
            }
            if (newUser.getBirthday() != null) {
                log.info("Обновление дня рождения пользователя");
                oldUser.setBirthday(newUser.getBirthday());
            }

            return oldUser;
        } else {
            log.error("Обновление пользователя: пользователь не найден");
            throw new UserNotFoundException("Пользователь с id: " + newUser.getId() + " не найден");
        }
    }
}
