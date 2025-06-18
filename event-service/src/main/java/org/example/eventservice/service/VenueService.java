package org.example.eventservice.service;

import lombok.RequiredArgsConstructor;
import org.example.eventservice.dto.CreateVenueDto;
import org.example.eventservice.dto.VenueResponseDto;
import org.example.eventservice.mapper.VenueMapper;
import org.example.eventservice.model.Venue;
import org.example.eventservice.repository.VenueRepository;
import org.example.commonlibrary.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class VenueService {
    private final VenueRepository venueRepository;
    private final VenueMapper venueMapper;

    public List<VenueResponseDto> getAllVenues() {
        return venueRepository.findAll().stream()
                .map(venueMapper::toDto)
                .toList();
    }

    public VenueResponseDto getVenueById(final UUID id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Venue not found"));
        return venueMapper.toDto(venue);
    }

    public VenueResponseDto createVenue(final CreateVenueDto dto) {
        Venue venue = venueMapper.toEntity(dto);
        Venue saved = venueRepository.save(venue);
        return venueMapper.toDto(saved);
    }

    public VenueResponseDto updateVenue(final UUID id, final CreateVenueDto dto) {
        Venue existingVenue = venueRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Venue not found"));
        Venue updatedVenue = venueMapper.toEntity(dto);
        updatedVenue.setVenueId(existingVenue.getVenueId());
        Venue saved = venueRepository.save(updatedVenue);
        return venueMapper.toDto(saved);
    }

    public VenueResponseDto deleteVenue(final UUID id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Venue not found"));
        venueRepository.delete(venue);
        return venueMapper.toDto(venue);
    }
}
