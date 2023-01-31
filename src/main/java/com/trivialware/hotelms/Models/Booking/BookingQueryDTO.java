package com.trivialware.hotelms.Models.Booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingQueryDTO {
    private Long hotelId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    private LocalDate bookingDateStart;

    private LocalDate bookingDateEnd;

    private Long userId;
}
