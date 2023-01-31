package com.trivialware.hotelms.Models.Booking;

import com.trivialware.hotelms.Enums.BookingStatus;
import com.trivialware.hotelms.Models.Room.RoomDTO;
import com.trivialware.hotelms.Models.ServiceHotel.ServiceHotelDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingDTO {
    private Long id;
    private Long userId;
    private Long hotelId;
    private LocalDate bookingDate;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int numberGuests;
    private BookingStatus bookingStatus;
    private BigDecimal price;
    private List<RoomDTO> rooms;
    private List<ServiceHotelDTO> services;


}
