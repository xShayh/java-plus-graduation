package ru.practicum.dto.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.util.DateTimeUtil;
import ru.practicum.validation.NullOrNotBlank;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventAdminRequestDto {
    @NullOrNotBlank
    @Size(min = 20, max = 2000)
    private String annotation;
    private Long category;
    @NullOrNotBlank
    @Size(min = 20, max = 7000)
    private String description;
    @Future
    @JsonFormat(pattern = DateTimeUtil.DATE_TIME_FORMAT)
    private LocalDateTime eventDate;
    private LocationDto location;
    private Boolean paid;
    private Long initiatorId;
    @PositiveOrZero
    private Long participantLimit;
    private Boolean requestModeration;
    @NullOrNotBlank
    @Size(min = 3, max = 120)
    private String title;
    private AdminEventState stateAction;
}
