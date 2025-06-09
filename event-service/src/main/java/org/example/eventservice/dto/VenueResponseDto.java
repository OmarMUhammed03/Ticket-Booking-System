package org.example.eventservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VenueResponseDto {
    private UUID venueId;
    private String name;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String contactPhone;
    private String contactEmail;
}
