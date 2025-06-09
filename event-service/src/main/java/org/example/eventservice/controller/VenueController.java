package org.example.eventservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.eventservice.dto.CreateVenueDto;
import org.example.eventservice.dto.VenueResponseDto;
import org.example.eventservice.service.VenueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/venues")
public class VenueController {
    private final VenueService venueService;

    @GetMapping
    public ResponseEntity<List<VenueResponseDto>> getAllVenues() {
        List<VenueResponseDto> venues = venueService.getAllVenues();
        return ResponseEntity.ok(venues);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VenueResponseDto> getVenueById(@PathVariable UUID id) {
        VenueResponseDto venue = venueService.getVenueById(id);
        return ResponseEntity.ok(venue);
    }

    @PostMapping
    public ResponseEntity<VenueResponseDto> createVenue(@RequestBody CreateVenueDto dto) {
        VenueResponseDto createdVenue = venueService.createVenue(dto);
        return new ResponseEntity<>(createdVenue, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VenueResponseDto> updateVenue(@PathVariable UUID id, @RequestBody CreateVenueDto dto) {
        VenueResponseDto updatedVenue = venueService.updateVenue(id, dto);
        return ResponseEntity.ok(updatedVenue);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<VenueResponseDto> deleteVenue(@PathVariable UUID id) {
        VenueResponseDto venue = venueService.deleteVenue(id);
        return ResponseEntity.ok(venue);
    }
}
