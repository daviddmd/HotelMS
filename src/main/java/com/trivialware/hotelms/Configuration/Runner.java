package com.trivialware.hotelms.Configuration;

import com.trivialware.hotelms.Entities.User;
import com.trivialware.hotelms.Enums.UserRole;
import com.trivialware.hotelms.Models.Hotel.HotelCreateDTO;
import com.trivialware.hotelms.Models.Room.RoomCreateDTO;
import com.trivialware.hotelms.Models.RoomType.RoomTypeCreateDTO;
import com.trivialware.hotelms.Models.ServiceHotel.ServiceHotelCreateDTO;
import com.trivialware.hotelms.Models.User.UserRegisterDTO;
import com.trivialware.hotelms.Models.User.UserUpdateAdminDTO;
import com.trivialware.hotelms.Services.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Profile("!test")
@ConditionalOnProperty(
        name = "application.runner",
        havingValue = "true")
@Component
@RequiredArgsConstructor
public class Runner implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(Runner.class);
    private final UserService userService;
    private final HotelService hotelService;

    private final RoomTypeService roomTypeService;

    private final RoomService roomService;

    private final BookingService bookingService;

    private final ServiceHotelService serviceHotelService;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        BigDecimal room1Cost, room2Cost, room3Cost, breakfastCost, parkingCost, spaCost;
        room1Cost = BigDecimal.valueOf(75);
        room2Cost = BigDecimal.valueOf(50);
        room3Cost = BigDecimal.valueOf(25);
        breakfastCost = BigDecimal.valueOf(15);
        parkingCost = BigDecimal.valueOf(25);
        spaCost = BigDecimal.valueOf(30);

        userService.register(UserRegisterDTO.builder().first_name("David").last_name("Duarte").username("admin").email("david.duarte@example.com").password("admin789").phoneNumber("910000000").build());
        userService.register(UserRegisterDTO.builder().first_name("Pedro").last_name("Santos").username("employee").email("pedro.santos@example.com").password("employee456").phoneNumber("920000000").build());
        userService.register(UserRegisterDTO.builder().first_name("Carlos").last_name("Sousa").username("customer1").email("carlos.sousa@example.com").password("customer123").phoneNumber("930000000").build());
        userService.register(UserRegisterDTO.builder().first_name("João").last_name("Félis").username("customer2").email("joao.felis@example.com").password("customer456").phoneNumber("940000000").build());
        userService.register(UserRegisterDTO.builder().first_name("José").last_name("Costa").username("customer3").email("jose.costa@example.com").password("customer789").phoneNumber("950000000").build());
        //Set Employee User Role to the Employee role
        User employee = userService.getUserById(2L);
        userService.updateUserAdmin(
                employee,
                UserUpdateAdminDTO.builder().
                        first_name(employee.getFirst_name()).
                        last_name(employee.getLast_name()).
                        email(employee.getEmail()).
                        phoneNumber(employee.getPhoneNumber()).
                        role(UserRole.EMPLOYEE).
                        enabled(true).
                        build()
        );
        hotelService.addHotel(HotelCreateDTO.builder().name("Hotel 1").code("HOTEL1").description("Hotel 1 Description").build());
        hotelService.addHotel(HotelCreateDTO.builder().name("Hotel 2").code("HOTEL2").description("Hotel 2 Description").build());
        hotelService.addHotel(HotelCreateDTO.builder().name("Hotel 3").code("HOTEL3").description("Hotel 3 Description").build());
        hotelService.addHotel(HotelCreateDTO.builder().name("Hotel 4").code("HOTEL4").description("Hotel 4 Description").build());
        roomTypeService.addRoomType(RoomTypeCreateDTO.builder().name("2 King-Sized Bed Suite").maximumCapacity(4).basePrice(new BigDecimal(40)).build());
        roomTypeService.addRoomType(RoomTypeCreateDTO.builder().name("3 King-Sized Bed Suite").maximumCapacity(6).basePrice(new BigDecimal(80)).build());
        roomTypeService.addRoomType(RoomTypeCreateDTO.builder().name("2 Single Bed Suite").maximumCapacity(2).basePrice(new BigDecimal(20)).build());
        roomService.addRoom(RoomCreateDTO.builder().code("ROOM1").name("Room 1").description("Room 1 on Floor 1").roomTypeID(2L).price(room1Cost).hotelID(1L).build());
        roomService.addRoom(RoomCreateDTO.builder().code("ROOM2").name("Room 2").description("Room 2 on Floor 1").roomTypeID(1L).price(room2Cost).hotelID(1L).build());
        roomService.addRoom(RoomCreateDTO.builder().code("ROOM3").name("Room 3").description("Room 3 on Floor 1").roomTypeID(3L).price(room3Cost).hotelID(1L).build());
        roomService.addRoom(RoomCreateDTO.builder().code("ROOM1").name("Room 1").description("Room 1 on Floor 1").roomTypeID(2L).price(room1Cost).hotelID(2L).build());
        roomService.addRoom(RoomCreateDTO.builder().code("ROOM2").name("Room 2").description("Room 2 on Floor 1").roomTypeID(1L).price(room2Cost).hotelID(2L).build());
        roomService.addRoom(RoomCreateDTO.builder().code("ROOM3").name("Room 2").description("Room 3 on Floor 1").roomTypeID(3L).price(room3Cost).hotelID(2L).build());
        serviceHotelService.addHotelService(ServiceHotelCreateDTO.builder().name("Breakfast").description("Mediterranean Breakfast from 08:00 to 11:00").code("BREAKFAST1").cost(breakfastCost).hotelId(1L).build());
        serviceHotelService.addHotelService(ServiceHotelCreateDTO.builder().name("Parking").description("Underground Parking Service").code("PARKING1").cost(parkingCost).hotelId(1L).build());
        serviceHotelService.addHotelService(ServiceHotelCreateDTO.builder().name("Spa").description("In-House Spa Service").code("SPA1").cost(spaCost).hotelId(1L).build());
        serviceHotelService.addHotelService(ServiceHotelCreateDTO.builder().name("Breakfast").description("Mediterranean Breakfast from 08:00 to 11:00").code("BREAKFAST1").cost(breakfastCost).hotelId(2L).build());
        serviceHotelService.addHotelService(ServiceHotelCreateDTO.builder().name("Parking").description("Underground Parking Service").code("PARKING1").cost(parkingCost).hotelId(2L).build());
        serviceHotelService.addHotelService(ServiceHotelCreateDTO.builder().name("Spa").description("In-House Spa Service").code("SPA1").cost(spaCost).hotelId(2L).build());
    }
}
