package ru.practicum.compilations.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.events.dto.EventShortDto;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompilationDto {
    @NotNull(message = "ID подборки обязателен")
    private Long id;

    @NotNull(message = "Флаг закрепления обязателен")
    private Boolean pinned;

    @NotNull(message = "Заголовок обязателен")
    private String title;

    private List<@NotNull EventShortDto> events;
}
