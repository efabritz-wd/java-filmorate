package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
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

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> findAll() {
        return userService.findAllUsers();
    }

    @GetMapping(value = "/{userId}")
    public User findUserById(@PathVariable int userId) {
        return userService.findUser(userId);
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        return userService.createUser(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        return userService.updateUser(newUser);
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
        User user = userService.findUser(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с id: " + id + "не найден.", User.class.getName());
        }
        return user.getFriends().stream()
                .map(userService::findUser)
                .toList();
    }

    @GetMapping(value = "/{id}/friends/common/{otherId}")
    public List<User> getUserFriends(@PathVariable int id, @PathVariable int otherId) {
        Set<Long> friendsIds = userService.getCommonFriends(id, otherId);
        return friendsIds.stream()
                .map(userService::findUser)
                .toList();
    }
}
