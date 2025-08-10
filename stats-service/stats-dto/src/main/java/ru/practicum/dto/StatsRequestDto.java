package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatsRequestDto {
    private String start;
    private String end;
    private List<String> uris;
    private Boolean unique;
}
