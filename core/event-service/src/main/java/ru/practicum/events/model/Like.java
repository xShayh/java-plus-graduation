package ru.practicum.events.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;


@Entity
@Table(name = "likes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Like {

    @EmbeddedId
    LikeId id;

    @ManyToOne
    @MapsId("eventId")
    @JoinColumn(name = "event_id", nullable = false)
    Event event;

    public Long getUserId() {
        return id != null ? id.getUserId() : null;
    }

    public Like(Long userId, Event event) {
        this.id = new LikeId(userId, event.getId());
        this.event = event;
    }
}
