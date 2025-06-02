package org.example.userservice.model;

import jakarta.persistence.*;
import lombok.Data;
import org.example.authservice.model.AuthUser;
import org.example.bookingservice.model.Booking;
import org.example.eventservice.model.Event;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    private UUID userId;
    @Column(unique = true)
    private String email;
    private String firstname;
    private String lastname;
    private LocalDate dateOfBirth;
    private String gender;
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private AuthUser authUser;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings = new ArrayList<>();
    @OneToMany(mappedBy = "creator")
    private List<Event> createdEvents = new ArrayList<>();
}

