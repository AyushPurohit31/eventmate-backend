package com.eventmate.event.repository;

import com.eventmate.event.model.VenueSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VenueSeatRepository extends JpaRepository<VenueSeat, UUID> {
}
