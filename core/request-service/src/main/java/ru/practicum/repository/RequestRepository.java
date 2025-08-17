package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.dto.request.RequestStatus;
import ru.practicum.model.Request;
import ru.practicum.model.RequestCount;


import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long>,
        QuerydslPredicateExecutor<Request> {
    List<Request> findAllByEventId(Long eventId);

    List<Request> findAllByRequesterId(Long userId);

    List<Request> findAllByRequesterIdAndEventId(Long userId, Long eventId);

    Boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);

    List<Request> findAllByEventIdAndStatus(Long eventId, RequestStatus status);

    @Query("SELECT new ru.practicum.ewm.model.ParticipationRequestCount(pr.eventId, count(pr.id)) " +
            "FROM ParticipationRequest pr WHERE pr.eventId in :ids and status = 'CONFIRMED' GROUP BY pr.eventId")
    List<RequestCount> getParticipationRequestCountConfirmed(@Param("ids") List<Long> ids);
}
