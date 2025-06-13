package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Film.
 */
@Data
public class Film {
    Long id;
    @NotBlank
    String name;
    @NotNull
    String description;
    @NotNull
    LocalDate releaseDate;
    @NotNull
    Long duration;

    private Set<Long> likes = new HashSet<>();

    public int getLikesAmount() {
        return this.likes.size();
    }
}
