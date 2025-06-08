package org.example.eventservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "events")
public class Event {
    @Id
    private UUID eventId;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String location;
    @Column(nullable = false)
    private LocalDateTime date;
    @Lob
    private String description;
    @Column(nullable = false)
    private UUID creatorId;
}

