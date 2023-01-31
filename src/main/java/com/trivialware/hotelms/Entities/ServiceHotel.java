package com.trivialware.hotelms.Entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@With
@Table(name = "services")
public class ServiceHotel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @NotEmpty
    private String name;

    @NotNull
    @NotEmpty
    private String description;

    @NotNull
    @NotEmpty
    private String code;
    //Cost per day of use
    @NotNull
    private BigDecimal cost;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "hotel_id")
    @JsonBackReference("hotel-service")
    private Hotel hotel;


}
