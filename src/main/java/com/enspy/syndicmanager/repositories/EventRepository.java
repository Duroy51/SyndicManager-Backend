package com.enspy.syndicmanager.repositories;

import com.enspy.syndicmanager.models.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    List<Event> findAllByOrderByStartDateDesc();
    List<Event> findByStartDateAfterOrderByStartDateAsc(LocalDateTime now);
    List<Event> findByStartDateBeforeOrderByStartDateDesc(LocalDateTime now);
}
