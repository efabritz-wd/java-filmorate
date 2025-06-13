package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.user.UserService;

import java.util.*;

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
    public List<User> findAll() {
        return userService.getUserStorage().findAllUsers();
    }

    @GetMapping(value = "/{userId}")
    public User findUserById(@PathVariable int userId) {
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

    @PutMapping(value = "/{id}/friends/{friendId}")
    public void addToFriends(@PathVariable int id, @PathVariable int friendId) {
        userService.addToFriends(id, friendId);
    }

    @DeleteMapping(value = "/{id}/friends/{friendId}")
    public void deleteFromFriends(@PathVariable int id, @PathVariable int friendId) {
        userService.deleteFromFriends(id, friendId);
    }

    @GetMapping(value = "/{id}/friends")
    public List<User> getUserFriends(@PathVariable int id) {
        User user = userService.getUserStorage().findUserById(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с id: " + id + "не найден.", User.class.getName());
        }
        return user.getFriends().stream()
                .map(userId -> userService.getUserStorage().findUserById(Math.toIntExact(userId)))
                .toList();
    }

    @GetMapping(value = "/{id}/friends/common/{otherId}")
    public List<User> getUserFriends(@PathVariable int id, @PathVariable int otherId) {
        Set<Long> friendsIds = userService.getCommonFriends(id, otherId);
        return friendsIds.stream()
                .map(userId -> userService.getUserStorage().findUserById(Math.toIntExact(userId)))
                .toList();
    }
}
