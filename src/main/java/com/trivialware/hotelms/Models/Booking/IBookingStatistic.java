package com.trivialware.hotelms.Models.Booking;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = BookingStatistic.class)
public interface IBookingStatistic {
    long getYear();

    long getMonth();

    String getHotelName();

    long getHotelId();

    long getBookingCount();
}
