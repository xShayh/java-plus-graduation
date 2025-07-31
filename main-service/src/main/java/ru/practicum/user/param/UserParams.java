package ru.practicum.user.param;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UserParams {
    private List<Integer> ids;
    private Integer from;
    private Integer size;
}
