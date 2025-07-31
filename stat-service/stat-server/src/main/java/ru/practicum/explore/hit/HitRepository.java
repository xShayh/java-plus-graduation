package ru.practicum.explore.hit;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HitRepository extends JpaRepository<EndpointHit, Long> {
}
