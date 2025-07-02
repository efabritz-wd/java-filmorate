package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Film.
 */
@Data
@Builder
public class Film {
    private Long id;
    @NotBlank
    private String name;
    @NotNull
    private String description;
    @NotNull
    private LocalDate releaseDate;
    @NotNull
    private Long duration;

    private MPA mpa;
    private Set<Genre> genres = new HashSet<>();

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Set<Long> likes = new HashSet<>();

    public int getLikesAmount() {
        if (this.likes == null) {
            return 0;
        }
        return this.likes.size();
    }
}
