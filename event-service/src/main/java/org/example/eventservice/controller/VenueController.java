package org.example.eventservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.eventservice.dto.CreateVenueDto;
import org.example.eventservice.dto.VenueResponseDto;
import org.example.eventservice.service.VenueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;
import java.util.UUID;

@Tag(name = "Venue Controller", description = "Endpoints for managing venues")
@RequiredArgsConstructor
@RestController
@RequestMapping("/venues")
public class VenueController {
    private final VenueService venueService;

    @Operation(summary = "Get all venues", description = "Fetches all venue records.")
    @GetMapping
    public ResponseEntity<List<VenueResponseDto>> getAllVenues() {
        List<VenueResponseDto> venues = venueService.getAllVenues();
        return ResponseEntity.ok(venues);
    }

    @Operation(summary = "Get venue by ID", description = "Retrieves a venue record by its unique identifier.")
    @GetMapping("/{id}")
    public ResponseEntity<VenueResponseDto> getVenueById(@PathVariable final UUID id) {
        VenueResponseDto venue = venueService.getVenueById(id);
        return ResponseEntity.ok(venue);
    }

    @Operation(summary = "Create a new venue",
            description = "Creates a new venue record based on the provided request data.")
    @PostMapping
    public ResponseEntity<VenueResponseDto> createVenue(@RequestBody final CreateVenueDto dto) {
        VenueResponseDto createdVenue = venueService.createVenue(dto);
        return new ResponseEntity<>(createdVenue, HttpStatus.CREATED);
    }

    @Operation(summary = "Update a venue",
            description = "Updates an existing venue record identified by its unique ID.")
    @PutMapping("/{id}")
    public ResponseEntity<VenueResponseDto> updateVenue(@PathVariable final UUID id,
                                                        @RequestBody final CreateVenueDto dto) {
        VenueResponseDto updatedVenue = venueService.updateVenue(id, dto);
        return ResponseEntity.ok(updatedVenue);
    }

    @Operation(summary = "Delete a venue", description = "Deletes a venue record by its unique identifier.")
    @DeleteMapping("/{id}")
    public ResponseEntity<VenueResponseDto> deleteVenue(@PathVariable final UUID id) {
        VenueResponseDto venue = venueService.deleteVenue(id);
        return ResponseEntity.ok(venue);
    }
}
