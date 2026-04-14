package com.eventmate.event.repository;

import com.eventmate.event.model.EventSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventScheduleRepository extends JpaRepository<EventSchedule, UUID> {
}
