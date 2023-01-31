package com.trivialware.hotelms.Models.Hotel;

import com.trivialware.hotelms.Models.Booking.BookingDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class HotelDTOAdmin extends HotelDTO {
    private boolean active;

    private List<BookingDTO> bookings;
}
