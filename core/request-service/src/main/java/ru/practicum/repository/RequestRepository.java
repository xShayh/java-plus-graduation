package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.model.Request;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByEvent_id(Long eventId);

    List<Request> findAllByRequester_Id(Long userId);

    @Query("SELECT r FROM Request r " +
            "JOIN r.event e " +
            "WHERE e.id = :eventId AND e.initiator.id = :userId")
    List<Request> findAllByRequester_IdAndEvent_id(Long userId, Long eventId);

    Boolean existsByRequesterAndEvent(Long requesterId, Long eventId);
}
