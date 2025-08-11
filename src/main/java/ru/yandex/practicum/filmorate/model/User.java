package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * User.
 */

@Data
@Builder
public class User {
    private Long id;
    @Email
    @NotNull
    private String email;
    @NotNull
    private String login;
    private String name;
    @NotNull
    private LocalDate birthday;

    private Set<Long> friends = new HashSet<>();
}
