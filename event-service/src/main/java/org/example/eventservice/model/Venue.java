package org.example.eventservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "venues")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venue {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID venueId;

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String address;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String city;

    @Size(max = 100)
    private String state;

    @Size(max = 20)
    private String postalCode;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String country;

    @Size(max = 25)
    private String contactPhone;

    @Email
    @Size(max = 100)
    private String contactEmail;

    @OneToMany(
            mappedBy = "venue",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<Event> events = new ArrayList<>();

    public void addEvent(Event event) {
        events.add(event);
    }
}