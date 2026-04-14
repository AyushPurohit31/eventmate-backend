package com.eventmate.event.service;

import com.eventmate.event.dto.RegisterVenueRequest;
import com.eventmate.event.dto.RegisterVenueSeatRequest;
import com.eventmate.event.dto.VenueResponse;
import com.eventmate.event.dto.VenueSeatResponse;
import com.eventmate.event.model.VenueSeat;
import com.eventmate.event.model.Venue;
import com.eventmate.event.repository.VenueRepository;
import com.eventmate.event.repository.VenueSeatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class VenueManagement {

    private final VenueRepository venueRepository;
    private final VenueSeatRepository venueSeatRepository;
    private static final Logger log  = LoggerFactory.getLogger(VenueManagement.class);

    public VenueManagement(VenueRepository venueRepository, VenueSeatRepository venueSeatRepository) {
        this.venueRepository = venueRepository;
        this.venueSeatRepository = venueSeatRepository;
    }

    @Transactional
    public VenueResponse registerVenue(RegisterVenueRequest request, UUID tenantId) {
        Venue venue = new Venue();
        venue.setTenantId(tenantId);
        venue.setName(request.getName());
        venue.setLatitude(request.getLatitude());
        venue.setLongitude(request.getLongitude());
        venue.setAddress(request.getAddress());
        venue.setCity(request.getCity());
        venue.setState(request.getState());
        venue.setCountry(request.getCountry());

        Venue savedVenue = venueRepository.save(venue);
        log.info("Venue registered successfully with id: {}", savedVenue.getId());
        return  VenueResponse.builder()
                .id(savedVenue.getId())
                .tenantId(savedVenue.getTenantId())
                .name(savedVenue.getName())
                .latitude(savedVenue.getLatitude())
                .longitude(savedVenue.getLongitude())
                .address(savedVenue.getAddress())
                .city(savedVenue.getCity())
                .state(savedVenue.getState())
                .country(savedVenue.getCountry())
                .build();
    }

    @Transactional
    public List<VenueSeatResponse> registerSeatingChart(UUID venueId, List<RegisterVenueSeatRequest> seatRequests) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new IllegalArgumentException("Venue not found: " + venueId));
        List<VenueSeat> seatEntities = new ArrayList<>();
        for (RegisterVenueSeatRequest seatRequest : seatRequests) {
            VenueSeat seat = new VenueSeat();
            seat.setVenue(venue);
            seat.setSection(seatRequest.getSection());
            seat.setRowNumber(seatRequest.getRowNumber());
            seat.setSeatNumber(seatRequest.getSeatNumber());
            seatEntities.add(seat);
        }
        List<VenueSeat> savedSeats = venueSeatRepository.saveAll(seatEntities);
        log.info("Seating chart registered successfully for venue: {}", venueId);
        List<VenueSeatResponse> responses = new ArrayList<>();
        for (VenueSeat savedSeat : savedSeats) {
            VenueSeatResponse response = new VenueSeatResponse();
            response.setId(savedSeat.getId());
            response.setSection(savedSeat.getSection());
            response.setRowNumber(savedSeat.getRowNumber());
            response.setSeatNumber(savedSeat.getSeatNumber());
            responses.add(response);
        }
        return responses;
    }
}
