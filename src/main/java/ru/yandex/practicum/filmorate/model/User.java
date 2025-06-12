package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * User.
 */

@Data
public class User {
    Integer id;
    @Email
    @NotNull
    String email;
    @NotNull
    String login;
    String name;
    @NotNull
    LocalDate birthday;

    private Set<Long> friends = new HashSet<>();
}
