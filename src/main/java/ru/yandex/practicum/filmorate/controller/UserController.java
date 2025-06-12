package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.user.UserService;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> findAll() {
        return userService.getUserStorage().findAllUsers();
    }

    @GetMapping(value = "/{userId}")
    public User findUserById(@RequestParam int userId) {
        return userService.getUserStorage().findUserById(userId);
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.getUserStorage().createUser(user);
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        return userService.getUserStorage().updateUser(newUser);
    }

    // PUT /users/{id}/friends/{friendId}
    @PutMapping(value = "/{id}/friends/{friendId}")
    public void addToFriends(int id, int friendId) {
        userService.addToFriends(id, friendId);
    }
    // DELETE /users/{id}/friends/{friendId}
    @DeleteMapping(value = "/{id}/friends/{friendId}")
    public void deleteFromFriends(int id, int friendId) {
        userService.deleteFromFriends(id, friendId);
    }
    // GET /users/{id}/friends
    @GetMapping(value = "/{id}/friends")
    public List<User> getUserFriends(int id) {
        User user = userService.getUserStorage().findUserById(id);
        if (user == null) {
            throw new UserNotFoundException("Пользователь с id: " + id + "не найден.");
        }
        return user.getFriends().stream()
                .map(userId -> userService.getUserStorage().findUserById(Math.toIntExact(userId)))
                .toList();
    }

    // GET /users/{id}/friends/common/{otherId}
    @GetMapping(value = "/{id}/friends/common/{otherId}")
    public List<User> getUserFriends(int id, int otherId) {
        Set<Long> friendsIds = userService.getCommonFriends(id, otherId);
        return friendsIds.stream()
                .map(userId -> userService.getUserStorage().findUserById(Math.toIntExact(userId)))
                .toList();
    }
}
