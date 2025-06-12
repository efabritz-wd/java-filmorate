package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private Integer id = 0;
    private final Map<Integer, User> usersMap = new HashMap<>();

    @Override
    public List<User> findAllUsers() {
        if (usersMap.values().isEmpty()) {
            return List.of();
        }

        return (List<User>) usersMap.values();
    }

    @Override
    public User findUserById(int id) {
        if (!usersMap.keySet().contains(id)) {
            throw new UserNotFoundException("Фильма с id " + id + " не существует");
        }
        return usersMap.get(id);
    }

    @Override
    public User createUser(User user) {
        if (!validateUser(user, true)) {
            throw new UserValidationException("Фильм не прошел валидацию при добавлении");
        }
        log.info("Новый пользователь прошел валидацию. Создание полей пользователя");
        user.setId(getNextId());

        usersMap.put(user.getId(), user);
        log.info("Новый пользователь добавлен");
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (user.getId() == null) {
            log.error("Обновление пользователя: не указан id");
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (!usersMap.containsKey(user.getId())) {
            log.error("Обновление пользователя: пользователь не найден");
            throw new UserNotFoundException("Пользователь с id: " + user.getId() + " не найден");
        }
        if (!validateUser(user, false)) {
            throw new UserValidationException("Фильм не прошел валидацию при добавлении");
        }
        if (usersMap.containsKey(user.getId())) {
            User oldUser = usersMap.get(user.getId());

            if (user.getName() != null) {
                log.info("Обновление имени пользователя");
                oldUser.setName(user.getName());
            }
            if (user.getEmail() != null) {
                log.info("Обновление мэйла пользователя");
                oldUser.setEmail(user.getEmail());
            }
            if (user.getLogin() != null) {
                log.info("Обновление логина пользователя");
                oldUser.setLogin(user.getLogin());
            }
            if (user.getBirthday() != null) {
                log.info("Обновление дня рождения пользователя");
                oldUser.setBirthday(user.getBirthday());
            }

            usersMap.put(user.getId(), user);
        }
        return user;
    }

    @Override
    public void deleteUser(User user) {
        if (!usersMap.keySet().contains(user.getId())) {
            throw new UserNotFoundException("Фильма с id " + user.getId() + " не существует");
        }
        usersMap.remove(user.getId());
        log.info("Пользователь с id " + user.getId() + " удален.");
    }

    private int getNextId() {
        int currentMaxId = usersMap.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
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
}
