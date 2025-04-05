package ru.practicum.explore.hit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HitRepository extends JpaRepository<EndpointHit, Long> {
}
