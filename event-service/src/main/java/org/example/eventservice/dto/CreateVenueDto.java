package org.example.eventservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateVenueDto {

    @NotBlank
    @Size(max = 150)
    private String name;

    @NotBlank
    private String address;

    @NotBlank
    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state;

    @Size(max = 20)
    private String postalCode;

    @NotBlank
    @Size(max = 100)
    private String country;

    @Size(max = 25)
    private String contactPhone;

    @Email
    @Size(max = 100)
    private String contactEmail;
}
