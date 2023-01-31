package com.trivialware.hotelms.Models.Booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingStatistics {
    private List<IBookingStatistic> bookingDateStatistics;

    private List<IBookingStatistic> checkInDateStatistics;
}
