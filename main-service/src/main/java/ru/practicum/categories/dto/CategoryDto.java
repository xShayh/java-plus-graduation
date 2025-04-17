package ru.practicum.categories.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryDto {
    private Integer id;
    @NotBlank
    @Size(min = 1, max = 50)
    private String name;
}
