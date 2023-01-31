package com.trivialware.hotelms.Repositories;

import com.trivialware.hotelms.Entities.Booking;
import com.trivialware.hotelms.Models.Booking.IBookingStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT " +
            "YEAR(booking.bookingDate) as year," +
            " MONTH(booking.bookingDate) AS month," +
            " booking.hotel.id AS hotelId," +
            " booking.hotel.name AS hotelName," +
            " COUNT(booking.id) AS bookingCount" +
            " FROM Booking booking" +
            " GROUP BY YEAR(booking.bookingDate), MONTH(booking.bookingDate), booking.hotel.id"
    )
    List<IBookingStatistic> groupByBookingDate();

    @Query("SELECT " +
            "YEAR(booking.checkInDate) as year," +
            " MONTH(booking.checkInDate) AS month," +
            " booking.hotel.id AS hotelId," +
            " booking.hotel.name AS hotelName," +
            " COUNT(booking.id) AS bookingCount" +
            " FROM Booking booking" +
            " GROUP BY YEAR(booking.checkInDate), MONTH(booking.checkInDate), booking.hotel.id"
    )
    List<IBookingStatistic> groupByCheckInDate();

}
