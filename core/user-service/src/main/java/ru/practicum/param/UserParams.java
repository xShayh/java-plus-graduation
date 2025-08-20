package ru.practicum.param;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UserParams {
    private List<Long> ids;
    private Integer from;
    private Integer size;
}
