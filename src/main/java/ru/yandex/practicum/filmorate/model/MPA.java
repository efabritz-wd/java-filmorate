package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MPA {
    private Long id;

    @NotBlank
    private String rating;
}
