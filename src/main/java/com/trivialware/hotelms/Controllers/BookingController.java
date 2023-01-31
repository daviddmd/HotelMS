package com.trivialware.hotelms.Controllers;

import com.trivialware.hotelms.Entities.Booking;
import com.trivialware.hotelms.Entities.User;
import com.trivialware.hotelms.Enums.UserRole;
import com.trivialware.hotelms.Exceptions.CustomException;
import com.trivialware.hotelms.Models.Booking.*;
import com.trivialware.hotelms.Models.Room.RoomDTO;
import com.trivialware.hotelms.Services.BookingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Tag(name = "Bookings")
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private final ModelMapper mapper;

    @PreAuthorize("hasAnyAuthority('ADMINISTRATOR','EMPLOYEE')")
    @GetMapping
    List<BookingDTO> getAllBookings(@RequestBody @Valid BookingQueryDTO dto) {
        return bookingService.getAllRange(dto).stream().map(booking -> mapper.map(booking, BookingDTO.class)).toList();
    }

    @PreAuthorize("hasAnyAuthority('ADMINISTRATOR','EMPLOYEE')")
    @GetMapping("/statistics")
    BookingStatistics getBookingStatistics() {
        return bookingService.getBookingStatistics();
    }

    @GetMapping("/{id}")
    BookingDTO getBooking(@PathVariable Long id, @AuthenticationPrincipal User user) {
        Booking booking = bookingService.getById(id);
        if (user.getRole() == UserRole.CUSTOMER && !booking.getUser().getId().equals(user.getId())) {
            throw new CustomException(String.format("No Booking with ID %d exists.", id), HttpStatus.NOT_FOUND);
        }
        return mapper.map(booking, BookingDTO.class);
    }

    @PostMapping()
    BookingDTO createBooking(@RequestBody @Valid BookingCreateDTO bookingCreateDTO, @AuthenticationPrincipal User user) {
        return mapper.map(bookingService.addBooking(user, bookingCreateDTO), BookingDTO.class);
    }

    @PreAuthorize("hasAnyAuthority('ADMINISTRATOR')")
    @DeleteMapping("/{id}")
    void deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(bookingService.getById(id));
    }

    @PutMapping("/{id}")
    BookingDTO updateBooking(@PathVariable Long id, @RequestBody @Valid BookingUpdateDTO bookingUpdateDTO, @AuthenticationPrincipal User user) {
        Booking booking = bookingService.getById(id);
        return mapper.map(bookingService.updateBooking(user, booking, bookingUpdateDTO), BookingDTO.class);
    }

    @GetMapping("/available-rooms")
    List<RoomDTO> getAvailableRoomsInDateRange(@RequestBody @Valid BookingAvailableRoomsDTO bookingAvailableRoomsDTO) {
        return bookingService.availableRooms(bookingAvailableRoomsDTO).
                stream().map(room -> mapper.map(room, RoomDTO.class)).toList();
    }

}
