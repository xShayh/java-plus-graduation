package ru.practicum.events.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.categories.model.Category;
import ru.practicum.events.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Integer> {

    @Query("SELECT e FROM Event e " +
            "WHERE (:users IS NULL OR e.initiator.id IN :users) " +
            "AND (:states IS NULL OR e.state IN :states) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND e.eventDate >= :rangeStart " +
            "AND e.eventDate <= :rangeEnd")
    List<Event> findAdminEvents(List<Integer> users,
                                List<String> states,
                                List<Integer> categories,
                                LocalDateTime rangeStart,
                                LocalDateTime rangeEnd,
                                Pageable pageble);

    Page<Event> findAllByInitiatorId(Integer userId, Pageable pageable);

    Event findByIdAndInitiatorId(Integer eventId, Integer userId);

    @Query("SELECT e FROM Event e " +
            "WHERE (:text IS NULL OR (e.title ILIKE :text " +
            "OR e.description ILIKE :text " +
            "OR e.annotation ILIKE :text)) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND e.eventDate >= :rangeStart " +
            "AND e.eventDate <= :rangeEnd " +
            "AND (:onlyAvailable IS NULL OR (e.state = 'PUBLISHED' " +
            "AND :onlyAvailable = true))")
    List<Event> findPublicEvents(String text,
                                 List<Integer> categories,
                                 Boolean paid,
                                 LocalDateTime rangeStart,
                                 LocalDateTime rangeEnd,
                                 Boolean onlyAvailable,
                                 Pageable pageable);

    Optional<Event> findByCategory(Category category);
}
