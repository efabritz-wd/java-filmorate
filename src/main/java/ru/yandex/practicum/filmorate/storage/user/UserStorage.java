package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    List<User> findAllUsers();

    User findUserById(long id);

    User createUser(User user);

    User updateUser(User user);

    void deleteUser(User user);
}
