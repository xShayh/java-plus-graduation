package ru.practicum.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RequestCountDto {
    Long eventId;
    Long quantity;
}
