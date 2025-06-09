package org.example.eventservice.service;

import org.example.eventservice.model.Venue;
import org.example.eventservice.repository.VenueRepository;
import org.example.commonexception.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class VenueService {
    private final VenueRepository venueRepository;

    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    public List<Venue> getAllVenues() {
        return venueRepository.findAll();
    }

    public Venue getVenueById(UUID id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Venue not found"));
    }

    public Venue createVenue(CreateVenueDto venue) {

        return venueRepository.save(venue);
    }

    public Venue updateVenue(UUID id, Venue updatedVenue) {
        Venue existingVenue = getVenueById(id);
        updatedVenue.setVenueId(existingVenue.getVenueId());
        return venueRepository.save(updatedVenue);
    }

    public void deleteVenue(UUID id) {
        Venue venue = getVenueById(id);
        venueRepository.delete(venue);
    }
}

