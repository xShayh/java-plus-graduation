package ru.practicum.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.dto.request.RequestStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime created;

    private Long eventId;

    Long requesterId;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;
}