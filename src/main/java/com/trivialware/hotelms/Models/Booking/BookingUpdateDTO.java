package com.trivialware.hotelms.Models.Booking;

import com.trivialware.hotelms.Enums.BookingStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class BookingUpdateDTO {
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
    @NotNull
    private BookingStatus bookingStatus;
    private BigDecimal price;
}
