package com.trivialware.hotelms.Models.Booking;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingCreateDTO {
    @NotNull
    private Long hotelId;
    @NotNull
    private LocalDate checkInDate;
    @NotNull
    private LocalDate checkOutDate;
    @NotNull
    @Min(0)
    private int numberGuests;

    @NotNull
    @NotEmpty
    private List<Long> roomIds;

    @NotNull
    private List<Long> serviceIds;

    private Long userId;


}
