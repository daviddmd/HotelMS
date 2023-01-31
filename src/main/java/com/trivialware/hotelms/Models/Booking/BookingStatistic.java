package com.trivialware.hotelms.Models.Booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingStatistic implements IBookingStatistic {
    private long year;
    private long month;
    private String hotelName;
    private long hotelId;
    private long bookingCount;
}
