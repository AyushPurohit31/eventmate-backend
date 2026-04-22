package com.eventmate.event.controller;

import com.eventmate.event.dto.RegisterVenueRequest;
import com.eventmate.event.dto.RegisterVenueSeatRequest;
import com.eventmate.event.dto.VenueResponse;
import com.eventmate.event.dto.VenueSeatResponse;
import com.eventmate.event.service.VenueManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/api/venue")
public class VenueController {

    private final VenueManagement venueManagement;
    private final Logger log  = LoggerFactory.getLogger(VenueController.class);

    public VenueController(VenueManagement venueManagement) {
        this.venueManagement = venueManagement;
    }

    @PostMapping("/register/{tenantId}")
    public ResponseEntity<VenueResponse> registerVenue(@PathVariable UUID tenantId, @RequestBody RegisterVenueRequest request) {
        log.info("Received request to register venue");
        VenueResponse response = venueManagement.registerVenue(request, tenantId);
        log.info("Venue registered successfully with id: {}", response.getId());
        return new ResponseEntity<>(response, org.springframework.http.HttpStatus.CREATED);
    }

    @PostMapping("/seating-chart/register/{venueId}")
    public ResponseEntity<List<VenueSeatResponse>> registerSeatingChart(@PathVariable UUID venueId, @RequestBody List<RegisterVenueSeatRequest> seatRequests) {
        log.info("Received request to register seating chart for venue: {}", venueId);
        List<VenueSeatResponse> responses = venueManagement.registerSeatingChart(venueId, seatRequests);
        return new ResponseEntity<>(responses, org.springframework.http.HttpStatus.CREATED);
    }
}
