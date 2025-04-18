package ru.practicum.request.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.request.model.Request;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Integer> {
    List<Request> findAllByEvent_id(Integer eventId);

    List<Request> findAllByRequester_Id(Integer userId);

    List<Request> findAllByRequester_IdAndEvent_id(Integer userId, Integer eventId);
}
