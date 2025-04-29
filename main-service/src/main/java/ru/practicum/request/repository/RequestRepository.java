package ru.practicum.request.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.events.model.Event;
import ru.practicum.request.model.Request;
import ru.practicum.user.model.User;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Integer> {
    List<Request> findAllByEvent_id(Integer eventId);

    List<Request> findAllByRequester_Id(Integer userId);

    @Query("SELECT r FROM Request r " +
            "JOIN r.event e " +
            "WHERE e.id = :eventId AND e.initiator.id = :userId")
    List<Request> findAllByRequester_IdAndEvent_id(Integer userId, Integer eventId);

    Boolean existsByRequesterAndEvent(User requester, Event event);
}
