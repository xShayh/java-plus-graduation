package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.model.Request;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByEventId(Long eventId);

    List<Request> findAllByRequesterId(Long userId);

    List<Request> findAllByRequesterIdAndEventId(Long userId, Long eventId);

    Boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);
}
