package ru.practicum.events.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.events.model.Like;
import ru.practicum.events.model.LikeId;


import java.util.List;

public interface LikeRepository extends JpaRepository<Like, LikeId> {

    boolean existsByIdUserIdAndIdEventId(Long userId, Long eventId);

    void deleteByIdUserIdAndIdEventId(Long userId, Long eventId);

    Long countByEventId(Long eventId);

    List<Like> findAllByEventId(Long eventId);

    List<Like> findAllByIdUserId(Long userId);
}
