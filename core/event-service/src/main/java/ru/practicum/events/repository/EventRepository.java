package ru.practicum.events.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.categories.model.Category;
import ru.practicum.events.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findAllByInitiatorId(Long userId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    @Query("SELECT e FROM Event e " +
            "WHERE (:users IS NULL OR e.initiator.id IN :users) " +
            "AND (:states IS NULL OR e.state IN :states) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND e.eventDate BETWEEN :rangeStart AND :rangeEnd")
    List<Event> findAdminEvents(List<Long> users,
                                List<String> states,
                                List<Long> categories,
                                LocalDateTime rangeStart,
                                LocalDateTime rangeEnd,
                                Pageable page);

    @Query("SELECT e FROM Event e " +
            "WHERE (:text IS NULL OR (e.title ILIKE :text " +
            "OR e.description ILIKE :text " +
            "OR e.annotation ILIKE :text)) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND e.eventDate BETWEEN :rangeStart AND :rangeEnd " +
            "AND (:onlyAvailable IS NULL OR e.state = 'PUBLISHED')")
    List<Event> findPublicEvents(String text,
                                 List<Long> categories,
                                 Boolean paid,
                                 LocalDateTime rangeStart,
                                 LocalDateTime rangeEnd,
                                 Boolean onlyAvailable,
                                 Pageable pageable);

    Optional<Event> findByCategory(Category category);

    List<Event> findAllByIdIn(List<Long> ids);
}
