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

    @Query("SELECT new ru.practicum.model.RequestCount(r.eventId, count(r.id)) " +
            "FROM Request r " +
            "WHERE r.eventId in :ids AND r.status = 'CONFIRMED' " +
            "GROUP BY r.eventId")
    List<RequestCount> getParticipationRequestCountConfirmed(@Param("ids") List<Long> ids);
}
