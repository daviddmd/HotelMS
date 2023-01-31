package com.trivialware.hotelms.Services;

import com.trivialware.hotelms.Entities.*;
import com.trivialware.hotelms.Enums.BookingStatus;
import com.trivialware.hotelms.Enums.UserRole;
import com.trivialware.hotelms.Exceptions.CustomException;
import com.trivialware.hotelms.Models.Booking.*;
import com.trivialware.hotelms.Repositories.BookingRepository;
import com.trivialware.hotelms.Repositories.RoomRepository;
import com.trivialware.hotelms.Repositories.ServiceHotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final ServiceHotelRepository serviceHotelRepository;
    private final UserService userService;

    private final HotelService hotelService;

    private final RoomRepository roomRepository;

    public Booking getById(Long id) {
        return bookingRepository.findById(id).orElseThrow(() -> new CustomException(String.format("No Booking with ID %d exists.", id), HttpStatus.NOT_FOUND));
    }

    public List<Booking> getAllRange(BookingQueryDTO dto) {
        Stream<Booking> bookingStream = bookingRepository.findAll().stream();
        if (dto.getHotelId() != null) {
            bookingStream = bookingStream.filter(booking -> booking.getHotel().getId().equals(dto.getHotelId()));
        }
        if (dto.getUserId() != null) {
            bookingStream = bookingStream.filter(booking -> booking.getUser().getId().equals(dto.getUserId()));
        }
        if (dto.getCheckInDate() != null && dto.getCheckOutDate() != null) {
            if (dto.getCheckInDate().isAfter(dto.getCheckOutDate())) {
                throw new CustomException(String.format("In the Check-in/out Date Range, the Check-in (%s) is After its Check-out (%s)", dto.getCheckInDate(), dto.getCheckOutDate()), HttpStatus.BAD_REQUEST);
            }
            bookingStream = bookingStream.filter(booking -> !booking.getCheckInDate().isAfter(dto.getCheckOutDate()) && !booking.getCheckOutDate().isBefore(dto.getCheckInDate()));
        }
        if (dto.getBookingDateStart() != null && dto.getBookingDateEnd() != null) {
            if (dto.getBookingDateStart().isAfter(dto.getBookingDateEnd())) {
                throw new CustomException(String.format("In the Booking Date Range, the Start (%s) is After its End (%s)", dto.getBookingDateStart(), dto.getBookingDateEnd()), HttpStatus.BAD_REQUEST);
            }
            bookingStream = bookingStream.filter(booking -> !booking.getBookingDate().isAfter(dto.getBookingDateEnd()) && !booking.getBookingDate().isBefore(dto.getBookingDateStart()));
        }
        return bookingStream.toList();
    }

    public BookingStatistics getBookingStatistics() {
        return BookingStatistics.builder().
                bookingDateStatistics(bookingRepository.groupByBookingDate()).
                checkInDateStatistics(bookingRepository.groupByCheckInDate()).
                build();
    }

    private Pair<List<Room>, List<ServiceHotel>> validateBookingRoomsAndServices(Hotel hotel, List<Long> bookingRoomIds, List<Long> bookingServiceIds, LocalDate checkInDate, LocalDate checkOutDate, int numberGuests) {
        //Rooms IDs of the Rooms in the Hotel
        Set<Long> hotelRoomIds = new HashSet<>(hotel.getRooms().stream().map(Room::getId).toList());
        Set<Long> hotelServiceIds = new HashSet<>(hotel.getServices().stream().map(ServiceHotel::getId).toList());
        if (hotelRoomIds.isEmpty()) {
            throw new CustomException(String.format("Hotel with ID %d doesn't have any rooms.", hotel.getId()), HttpStatus.BAD_REQUEST);
        }
        if (!bookingServiceIds.isEmpty() && hotelServiceIds.isEmpty()) {
            throw new CustomException(String.format("Hotel with ID %d doesn't have any services.", hotel.getId()), HttpStatus.BAD_REQUEST);
        }
        if (bookingRoomIds.isEmpty()) {
            throw new CustomException("No rooms specified in booking.", HttpStatus.BAD_REQUEST);
        }
        if (!hotelRoomIds.containsAll(bookingRoomIds)) {
            throw new CustomException(String.format("Invalid Room ID passed for Hotel with ID %d", hotel.getId()), HttpStatus.BAD_REQUEST);
        }
        if (!hotelServiceIds.containsAll(bookingServiceIds)) {
            throw new CustomException(String.format("Invalid Service ID passed for Hotel with ID %d", hotel.getId()), HttpStatus.BAD_REQUEST);
        }
        if (checkInDate.isAfter(checkOutDate)) {
            throw new CustomException("Check-in Date After Check-Out Date.", HttpStatus.BAD_REQUEST);
        }

        List<ServiceHotel> bookingServices = serviceHotelRepository.findByIdIn(bookingServiceIds);
        //Check for rooms that are booked between the check-in date and check-out date
        List<Room> bookedRooms = bookedRooms(hotel, checkInDate.plusDays(1), checkOutDate.minusDays(1));
        //List of Rooms that will be booked in this booking
        List<Room> bookingRooms = roomRepository.findByIdIn(bookingRoomIds);
        /*
        Disjoint set between the rooms that are already booked in the date range and the rooms that are to be booked
        If there are elements in common in the bookingRooms list (list with the rooms id that are present in the booking)
        and the roomsInBookingInDateRange (rooms that are already booked in the booking date range), there's an overlap
        and the booking isn't logically possible
         */
        if (!Collections.disjoint(bookedRooms, bookingRooms)) {
            throw new CustomException("Some rooms in the booking are already booked for this date range.", HttpStatus.BAD_REQUEST);
        }
        int maximumNumberGuestsBookingRooms = bookingRooms.stream().mapToInt(room -> room.getRoomType().getMaximumCapacity()).sum();
        if (maximumNumberGuestsBookingRooms < numberGuests) {
            throw new CustomException(
                    String.format("The selected Booking Rooms cumulative capacity (%d) is fewer than the booked number of guests (%d).",
                            maximumNumberGuestsBookingRooms, numberGuests),
                    HttpStatus.BAD_REQUEST);
        }
        return Pair.of(bookingRooms, bookingServices);
    }

    public Booking addBooking(User loggedInUser, BookingCreateDTO bookingCreateDTO) {
        /*
          If the logged-in user isn't a regular customer and the user ID in the DTO isn't null, set the booking user
          as the user with such ID, otherwise set it to the currently logged-in user
         */
        User bookingUser = loggedInUser.getRole() != UserRole.CUSTOMER && bookingCreateDTO.getUserId() != null ?
                userService.getUserById(bookingCreateDTO.getUserId()) :
                loggedInUser;
        Hotel hotel = hotelService.getById(bookingCreateDTO.getHotelId());
        Pair<List<Room>, List<ServiceHotel>> pair = validateBookingRoomsAndServices(
                hotel,
                bookingCreateDTO.getRoomIds(),
                bookingCreateDTO.getServiceIds(),
                bookingCreateDTO.getCheckInDate(),
                bookingCreateDTO.getCheckOutDate(),
                bookingCreateDTO.getNumberGuests()
        );
        List<Room> bookingRooms = pair.getFirst();
        List<ServiceHotel> bookingServices = pair.getSecond();
        BigDecimal price = calculateBookingCost(
                bookingRooms,
                bookingServices,
                DAYS.between(bookingCreateDTO.getCheckInDate(), bookingCreateDTO.getCheckOutDate())).setScale(2, RoundingMode.HALF_UP);
        Booking booking = Booking.builder().
                user(bookingUser).
                hotel(hotel).
                bookingDate(LocalDate.now()).
                checkInDate(bookingCreateDTO.getCheckInDate()).
                checkOutDate(bookingCreateDTO.getCheckOutDate()).
                numberGuests(bookingCreateDTO.getNumberGuests()).bookingStatus(BookingStatus.PENDING).
                price(price).rooms(bookingRooms).
                services(bookingServices).
                build();
        return bookingRepository.save(booking);
    }

    public Booking updateBooking(User loggedInUser, Booking booking, BookingUpdateDTO bookingUpdateDTO) {
        if (loggedInUser.getRole() == UserRole.CUSTOMER) {
            if (!booking.getUser().getId().equals(loggedInUser.getId())) {
                throw new CustomException(String.format("No Booking with ID %d exists.", booking.getId()), HttpStatus.NOT_FOUND);
            }
            if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
                throw new CustomException("This booking is cancelled and can't be updated.", HttpStatus.BAD_REQUEST);
            }
            else if (booking.getBookingStatus() == BookingStatus.COMPLETED) {
                throw new CustomException("This booking is completed and can't be updated.", HttpStatus.BAD_REQUEST);
            }
        }
        Hotel hotel = booking.getHotel();
        booking.setRooms(List.of());
        Pair<List<Room>, List<ServiceHotel>> pair = validateBookingRoomsAndServices(
                hotel,
                bookingUpdateDTO.getRoomIds(),
                bookingUpdateDTO.getServiceIds(),
                bookingUpdateDTO.getCheckInDate(),
                bookingUpdateDTO.getCheckOutDate(),
                bookingUpdateDTO.getNumberGuests()
        );
        List<Room> bookingRooms = pair.getFirst();
        List<ServiceHotel> bookingServices = pair.getSecond();
        BigDecimal bookingCost = calculateBookingCost(
                bookingRooms,
                bookingServices,
                DAYS.between(bookingUpdateDTO.getCheckInDate(), bookingUpdateDTO.getCheckOutDate()));
        /*
        The customer may only change the booking status to Cancelled
         */
        if (loggedInUser.getRole() == UserRole.CUSTOMER) {
            if (bookingUpdateDTO.getBookingStatus() == BookingStatus.CANCELLED) {
                booking.setBookingStatus(BookingStatus.CANCELLED);
            }
        }
        else {
            booking.setBookingStatus(bookingUpdateDTO.getBookingStatus());
        }
        //Only non-customer users can update the booking price. If the price is not specified (null), it is calculated
        booking.setPrice(loggedInUser.getRole() != UserRole.CUSTOMER && bookingUpdateDTO.getPrice() != null ?
                bookingUpdateDTO.getPrice().setScale(2, RoundingMode.HALF_UP) :
                bookingCost.setScale(2, RoundingMode.HALF_UP)
        );
        booking.setRooms(bookingRooms);
        booking.setCheckInDate(bookingUpdateDTO.getCheckInDate());
        booking.setCheckOutDate(bookingUpdateDTO.getCheckOutDate());
        booking.setNumberGuests(bookingUpdateDTO.getNumberGuests());
        return bookingRepository.save(booking);
    }

    public List<Room> availableRooms(BookingAvailableRoomsDTO dto) {
        if (dto.getCheckInDate().isAfter(dto.getCheckOutDate())) {
            throw new CustomException("Check-in Date After Check-out Date", HttpStatus.BAD_REQUEST);
        }
        Hotel hotel = hotelService.getById(dto.getHotelId());
        return availableRooms(hotel, dto.getCheckInDate(), dto.getCheckOutDate());
    }

    private List<Room> availableRooms(Hotel hotel, LocalDate checkInDate, LocalDate checkOutDate) {
        List<Room> hotelRooms = hotel.getRooms();
        //fixme
        List<Room> bookedRooms = bookedRooms(hotel, checkInDate.plusDays(1), checkOutDate.minusDays(1));
        return hotelRooms.stream().filter(room -> !bookedRooms.contains(room)).toList();
    }

    private List<Room> bookedRooms(Hotel hotel, LocalDate checkInDate, LocalDate checkOutDate) {
        //Bookings that overlap with the given date range
        List<Booking> bookings = bookingRepository.findAll().stream().
                filter(booking -> booking.getHotel().equals(hotel) && !booking.getCheckInDate().isAfter(checkOutDate) && !booking.getCheckOutDate().isBefore(checkInDate)).toList();
        return bookings.stream().flatMap(booking -> booking.getRooms().stream()).toList();
    }

    private BigDecimal calculateBookingCost(List<Room> bookingRooms, List<ServiceHotel> bookingServices, long numberDays) {
        BigDecimal roomsCost = bookingRooms.stream().map(Room::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add).multiply(BigDecimal.valueOf(numberDays));
        BigDecimal servicesCost = bookingServices.stream().map(ServiceHotel::getCost).reduce(BigDecimal.ZERO, BigDecimal::add).multiply(BigDecimal.valueOf(numberDays + 1));
        return roomsCost.add(servicesCost);
    }

    public void deleteBooking(Booking booking) {
        booking.setServices(List.of());
        booking.setRooms(List.of());
        bookingRepository.delete(booking);
    }
}
