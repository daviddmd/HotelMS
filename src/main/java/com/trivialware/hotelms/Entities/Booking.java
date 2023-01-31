package com.trivialware.hotelms.Entities;

import com.trivialware.hotelms.Enums.BookingStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@With
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference(value = "user-booking")
    private User user;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "hotel_id")
    @JsonBackReference(value = "hotel-booking")
    private Hotel hotel;
    @NotNull

    private LocalDate bookingDate;
    @NotNull

    private LocalDate checkInDate;
    @NotNull
    private LocalDate checkOutDate;
    @NotNull
    @Min(0)
    private int numberGuests;

    @NotNull
    @Enumerated(EnumType.STRING)
    private BookingStatus bookingStatus;
    @NotNull
    @Min(0)
    private BigDecimal price;

    @ManyToMany(cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Room> rooms;

    @ManyToMany(cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<ServiceHotel> services;

}
