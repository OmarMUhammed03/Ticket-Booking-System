package org.example.eventservice.mapper;

import org.example.eventservice.dto.CreateVenueDto;
import org.example.eventservice.dto.VenueResponseDto;
import org.example.eventservice.model.Venue;
import org.springframework.stereotype.Component;

@Component
public class VenueMapper {
    public VenueResponseDto toDto(Venue venue) {
        return new VenueResponseDto(
                venue.getVenueId(),
                venue.getName(),
                venue.getAddress(),
                venue.getCity(),
                venue.getState(),
                venue.getPostalCode(),
                venue.getCountry(),
                venue.getContactPhone(),
                venue.getContactEmail()
        );
    }

    public Venue toEntity(CreateVenueDto dto) {
        return Venue.builder()
                .name(dto.getName())
                .address(dto.getAddress())
                .city(dto.getCity())
                .state(dto.getState())
                .postalCode(dto.getPostalCode())
                .country(dto.getCountry())
                .contactPhone(dto.getContactPhone())
                .contactEmail(dto.getContactEmail())
                .build();
    }
}

