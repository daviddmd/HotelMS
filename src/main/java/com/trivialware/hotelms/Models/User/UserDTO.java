package com.trivialware.hotelms.Models.User;

import com.trivialware.hotelms.Models.Booking.BookingDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String first_name;
    private String last_name;
    private String email;
    private String username;
    private String phoneNumber;
    private List<BookingDTO> bookings;
}
