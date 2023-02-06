package com.trivialware.hotelms.Entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.ColumnDefault;

import java.util.List;
import java.util.Objects;

@Entity
@ToString
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@With
@Table(name = "hotels")
public class Hotel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotEmpty
    @NotNull
    private String name;
    @NotEmpty
    @NotNull
    @Column(unique = true)
    private String code;
    private String description;
    @ColumnDefault("true")
    @NotNull
    private boolean active;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("hotel-room")
    @ToString.Exclude
    private List<Room> rooms;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("hotel-service")
    @ToString.Exclude
    private List<ServiceHotel> services;
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "hotel-booking")
    @ToString.Exclude
    private List<Booking> bookings;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Hotel hotel = (Hotel) o;
        return id != null && Objects.equals(id, hotel.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }


}
