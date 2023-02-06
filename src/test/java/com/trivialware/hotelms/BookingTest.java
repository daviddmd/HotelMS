package com.trivialware.hotelms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trivialware.hotelms.Entities.User;
import com.trivialware.hotelms.Enums.BookingStatus;
import com.trivialware.hotelms.Enums.UserRole;
import com.trivialware.hotelms.Models.Booking.*;
import com.trivialware.hotelms.Models.Hotel.HotelCreateDTO;
import com.trivialware.hotelms.Models.Hotel.HotelDTOAdmin;
import com.trivialware.hotelms.Models.Room.RoomCreateDTO;
import com.trivialware.hotelms.Models.Room.RoomDTO;
import com.trivialware.hotelms.Models.RoomType.RoomTypeCreateDTO;
import com.trivialware.hotelms.Models.ServiceHotel.ServiceHotelCreateDTO;
import com.trivialware.hotelms.Models.ServiceHotel.ServiceHotelDTO;
import com.trivialware.hotelms.Models.User.*;
import com.trivialware.hotelms.Services.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static com.trivialware.hotelms.Helper.*;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = HotelMsApplication.class)
@ContextConfiguration(classes = HotelMsApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-testing.properties")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class BookingTest {
    ObjectMapper mapper;
    String adminToken, employeeToken, firstCustomerToken, secondCustomerToken;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserService userService;
    @Autowired
    private HotelService hotelService;
    @Autowired
    private RoomService roomService;
    @Autowired
    private RoomTypeService roomTypeService;
    @Autowired
    private ServiceHotelService serviceHotelService;
    private BigDecimal room1Cost, room2Cost, room3Cost, breakfastCost, parkingCost, spaCost;

    @BeforeAll
    void setup() {
        room1Cost = BigDecimal.valueOf(75);
        room2Cost = BigDecimal.valueOf(50);
        room3Cost = BigDecimal.valueOf(25);
        breakfastCost = BigDecimal.valueOf(15);
        parkingCost = BigDecimal.valueOf(25);
        spaCost = BigDecimal.valueOf(30);
        userService.register(UserRegisterDTO.builder().first_name("David").last_name("Duarte").username(usernameAdmin).email(emailAdmin).password(passwordAdmin).phoneNumber("910000000").build());
        userService.register(UserRegisterDTO.builder().first_name("Pedro").last_name("Santos").username(usernameEmployee).email(emailEmployee).password(passwordEmployee).phoneNumber("920000000").build());
        userService.register(UserRegisterDTO.builder().first_name("Carlos").last_name("Sousa").username(usernameFirstCustomer).email(emailFirstCustomer).password(passwordFirstCustomer).phoneNumber("930000000").build());
        userService.register(UserRegisterDTO.builder().first_name("João").last_name("Félis").username(usernameSecondCustomer).email(emailSecondCustomer).password(passwordSecondCustomer).phoneNumber("940000000").build());
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
        mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        adminToken = userService.login(UserLoginDTO.builder().usernameOrEmail(emailAdmin).password(passwordAdmin).rememberMe(true).build());
        employeeToken = userService.login(UserLoginDTO.builder().usernameOrEmail(usernameEmployee).password(passwordEmployee).rememberMe(true).build());
        firstCustomerToken = userService.login(UserLoginDTO.builder().usernameOrEmail(emailFirstCustomer).password(passwordFirstCustomer).rememberMe(true).build());
        secondCustomerToken = userService.login(UserLoginDTO.builder().usernameOrEmail(emailSecondCustomer).password(passwordSecondCustomer).rememberMe(true).build());

    }

    @Test
    @Order(1)
    void createBookings() throws Exception {
        /*
         * Customer 1:
         *  Hotel 1: 2023/3/1 -> 2023/3/5 -> Rooms 2,3 (5 people)
         *           2023/3/7 -> 2023/3/12 -> Room 1 (6 people)
         *  Hotel 2: 2023/3/25 -> 2023/4/10 -> Rooms 6 (2 people)
         * Customer 2:
         *  Hotel 1: 2023/2/25 -> 2023/3/4 -> Room 1 (4 people)
         *  Hotel 2: 2023/3/20 -> 2023/3/25 -> Rooms 4,6 (8 people)
         *           2023/3/29 -> 2023/4/2 -> Room 5 (4 people)
         * Customer 3:
         *  Hotel 1 : 2023/5/1 -> 2023/5/10 -> Room 2 (4 people)
         *  Hotel 2:  2023/6/10 -> 2023/6/20 -> Room 5 (4 people)
         */
        //Assert that there are no bookings in Hotel 1 and 2, as well as in the booking controller
        long numberDays;
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/hotels/1").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        HotelDTOAdmin hotelDTOAdmin = fromJson(mapper, result, HotelDTOAdmin.class);
        assertEquals(0, hotelDTOAdmin.getBookings().size());
        result = mvc.perform(MockMvcRequestBuilders.get("/hotels/2").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        hotelDTOAdmin = fromJson(mapper, result, HotelDTOAdmin.class);
        assertEquals(0, hotelDTOAdmin.getBookings().size());
        result = mvc.perform(MockMvcRequestBuilders.get("/bookings").
                        content(toJson(mapper, BookingQueryDTO.builder().build())).
                        characterEncoding(StandardCharsets.UTF_8).
                        contentType(MediaType.APPLICATION_JSON).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        List<BookingDTO> bookingDTOList = listFromJson(mapper, result, BookingDTO[].class);
        assertEquals(0, bookingDTOList.size());
        //Create Bookings for First Customer
        BookingCreateDTO bookingCreateDTO = BookingCreateDTO.builder().
                hotelId(1L).
                checkInDate(LocalDate.of(2023, 3, 1)).
                checkOutDate(LocalDate.of(2023, 3, 5)).
                numberGuests(5).
                roomIds(List.of(2L, 3L)).
                serviceIds(List.of(1L, 3L)).
                build();
        result = mvc.perform(MockMvcRequestBuilders.post("/bookings").
                        content(toJson(mapper, bookingCreateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + firstCustomerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).
                andReturn();
        BookingDTO bookingDTO = fromJson(mapper, result, BookingDTO.class);
        numberDays = DAYS.between(bookingCreateDTO.getCheckInDate(), bookingCreateDTO.getCheckOutDate());
        BigDecimal estimatedCostRooms = (room2Cost.add(room3Cost)).multiply(BigDecimal.valueOf(numberDays));
        BigDecimal estimatedCostServices = (breakfastCost.add(spaCost)).multiply(BigDecimal.valueOf(numberDays + 1));
        assertEquals(estimatedCostRooms.add(estimatedCostServices).setScale(2, RoundingMode.HALF_UP), bookingDTO.getPrice());
        assertEquals(BookingStatus.PENDING, bookingDTO.getBookingStatus());
        assertEquals(1, bookingDTO.getHotelId());
        assertEquals(5, bookingDTO.getNumberGuests());
        assertTrue(bookingDTO.getRooms().stream().map(RoomDTO::getId).toList().containsAll(List.of(2L, 3L)));
        assertTrue(bookingDTO.getServices().stream().map(ServiceHotelDTO::getId).toList().containsAll(List.of(1L, 3L)));
        bookingCreateDTO = BookingCreateDTO.builder().
                hotelId(1L).
                checkInDate(LocalDate.of(2023, 3, 7)).
                checkOutDate(LocalDate.of(2023, 3, 12)).
                numberGuests(6).
                roomIds(List.of(1L)).
                serviceIds(List.of(1L, 2L, 3L)).
                build();
        result = mvc.perform(MockMvcRequestBuilders.post("/bookings").
                        content(toJson(mapper, bookingCreateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + firstCustomerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).
                andReturn();
        bookingDTO = fromJson(mapper, result, BookingDTO.class);
        numberDays = DAYS.between(bookingCreateDTO.getCheckInDate(), bookingCreateDTO.getCheckOutDate());
        estimatedCostRooms = room1Cost.multiply(BigDecimal.valueOf(numberDays));
        estimatedCostServices = (breakfastCost.add(parkingCost).add(spaCost)).multiply(BigDecimal.valueOf(numberDays + 1));
        assertEquals(estimatedCostRooms.add(estimatedCostServices).setScale(2, RoundingMode.HALF_UP), bookingDTO.getPrice());
        bookingCreateDTO = BookingCreateDTO.builder().
                hotelId(2L).
                checkInDate(LocalDate.of(2023, 3, 25)).
                checkOutDate(LocalDate.of(2023, 4, 10)).
                numberGuests(2).
                roomIds(List.of(6L)).
                serviceIds(List.of(5L)).
                build();
        result = mvc.perform(MockMvcRequestBuilders.post("/bookings").
                        content(toJson(mapper, bookingCreateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + firstCustomerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).
                andReturn();
        bookingDTO = fromJson(mapper, result, BookingDTO.class);
        numberDays = DAYS.between(bookingCreateDTO.getCheckInDate(), bookingCreateDTO.getCheckOutDate());
        estimatedCostRooms = room3Cost.multiply(BigDecimal.valueOf(numberDays));
        estimatedCostServices = parkingCost.multiply(BigDecimal.valueOf(numberDays + 1));
        assertEquals(estimatedCostRooms.add(estimatedCostServices).setScale(2, RoundingMode.HALF_UP), bookingDTO.getPrice());

        //Create Bookings for Second Customer
        bookingCreateDTO = BookingCreateDTO.builder().
                hotelId(1L).
                checkInDate(LocalDate.of(2023, 2, 25)).
                checkOutDate(LocalDate.of(2023, 3, 4)).
                numberGuests(4).
                roomIds(List.of(1L)).
                serviceIds(List.of(1L, 2L)).
                build();
        result = mvc.perform(MockMvcRequestBuilders.post("/bookings").
                        content(toJson(mapper, bookingCreateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + secondCustomerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).
                andReturn();
        bookingDTO = fromJson(mapper, result, BookingDTO.class);
        numberDays = DAYS.between(bookingCreateDTO.getCheckInDate(), bookingCreateDTO.getCheckOutDate());
        estimatedCostRooms = room1Cost.multiply(BigDecimal.valueOf(numberDays));
        estimatedCostServices = (breakfastCost.add(parkingCost)).multiply(BigDecimal.valueOf(numberDays + 1));
        assertEquals(estimatedCostRooms.add(estimatedCostServices).setScale(2, RoundingMode.HALF_UP), bookingDTO.getPrice());

        bookingCreateDTO = BookingCreateDTO.builder().
                hotelId(2L).
                checkInDate(LocalDate.of(2023, 3, 20)).
                checkOutDate(LocalDate.of(2023, 3, 25)).
                numberGuests(8).
                roomIds(List.of(4L, 6L)).
                serviceIds(List.of(5L, 6L)).
                build();
        result = mvc.perform(MockMvcRequestBuilders.post("/bookings").
                        content(toJson(mapper, bookingCreateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + secondCustomerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).
                andReturn();
        bookingDTO = fromJson(mapper, result, BookingDTO.class);
        numberDays = DAYS.between(bookingCreateDTO.getCheckInDate(), bookingCreateDTO.getCheckOutDate());
        estimatedCostRooms = (room1Cost.add(room3Cost)).multiply(BigDecimal.valueOf(numberDays));
        estimatedCostServices = (parkingCost.add(spaCost)).multiply(BigDecimal.valueOf(numberDays + 1));
        assertEquals(estimatedCostRooms.add(estimatedCostServices).setScale(2, RoundingMode.HALF_UP), bookingDTO.getPrice());

        bookingCreateDTO = BookingCreateDTO.builder().
                hotelId(2L).
                checkInDate(LocalDate.of(2023, 3, 29)).
                checkOutDate(LocalDate.of(2023, 4, 2)).
                numberGuests(4).
                roomIds(List.of(5L)).
                serviceIds(List.of(4L, 5L, 6L)).
                build();
        result = mvc.perform(MockMvcRequestBuilders.post("/bookings").
                        content(toJson(mapper, bookingCreateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + secondCustomerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).
                andReturn();
        bookingDTO = fromJson(mapper, result, BookingDTO.class);
        numberDays = DAYS.between(bookingCreateDTO.getCheckInDate(), bookingCreateDTO.getCheckOutDate());
        estimatedCostRooms = room2Cost.multiply(BigDecimal.valueOf(numberDays));
        estimatedCostServices = (breakfastCost.add(parkingCost).add(spaCost)).multiply(BigDecimal.valueOf(numberDays + 1));
        assertEquals(estimatedCostRooms.add(estimatedCostServices).setScale(2, RoundingMode.HALF_UP), bookingDTO.getPrice());

        result = mvc.perform(MockMvcRequestBuilders.get("/users/self").
                        header("Authorization", "Bearer " + firstCustomerToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        UserDTO userDTO = fromJson(mapper, result, UserDTO.class);
        assertTrue(userDTO.getBookings().stream().map(BookingDTO::getId).toList().containsAll(List.of(1L, 2L, 3L)));
        assertEquals(3, userDTO.getBookings().size());
        result = mvc.perform(MockMvcRequestBuilders.get("/users/self").
                        header("Authorization", "Bearer " + secondCustomerToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        userDTO = fromJson(mapper, result, UserDTO.class);
        assertTrue(userDTO.getBookings().stream().map(BookingDTO::getId).toList().containsAll(List.of(4L, 5L, 6L)));
        assertEquals(3, userDTO.getBookings().size());

        //Create Bookings for Customer 3 as an Employee/Administrator

        bookingCreateDTO = BookingCreateDTO.builder().
                hotelId(1L).
                checkInDate(LocalDate.of(2023, 5, 1)).
                checkOutDate(LocalDate.of(2023, 5, 10)).
                numberGuests(4).
                roomIds(List.of(2L)).
                serviceIds(List.of(1L, 2L, 3L)).
                userId(5L).
                build();
        result = mvc.perform(MockMvcRequestBuilders.post("/bookings").
                        content(toJson(mapper, bookingCreateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + employeeToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).
                andReturn();
        bookingDTO = fromJson(mapper, result, BookingDTO.class);
        numberDays = DAYS.between(bookingCreateDTO.getCheckInDate(), bookingCreateDTO.getCheckOutDate());
        estimatedCostRooms = room2Cost.multiply(BigDecimal.valueOf(numberDays));
        estimatedCostServices = (breakfastCost.add(parkingCost).add(spaCost)).multiply(BigDecimal.valueOf(numberDays + 1));
        assertEquals(estimatedCostRooms.add(estimatedCostServices).setScale(2, RoundingMode.HALF_UP), bookingDTO.getPrice());

        bookingCreateDTO = BookingCreateDTO.builder().
                hotelId(2L).
                checkInDate(LocalDate.of(2023, 6, 10)).
                checkOutDate(LocalDate.of(2023, 6, 20)).
                numberGuests(4).
                roomIds(List.of(5L)).
                serviceIds(List.of(4L, 5L, 6L)).
                userId(5L).
                build();
        result = mvc.perform(MockMvcRequestBuilders.post("/bookings").
                        content(toJson(mapper, bookingCreateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).
                andReturn();
        bookingDTO = fromJson(mapper, result, BookingDTO.class);
        numberDays = DAYS.between(bookingCreateDTO.getCheckInDate(), bookingCreateDTO.getCheckOutDate());
        estimatedCostRooms = room2Cost.multiply(BigDecimal.valueOf(numberDays));
        estimatedCostServices = (breakfastCost.add(parkingCost).add(spaCost)).multiply(BigDecimal.valueOf(numberDays + 1));
        assertEquals(estimatedCostRooms.add(estimatedCostServices).setScale(2, RoundingMode.HALF_UP), bookingDTO.getPrice());
    }

    @Test
    @Order(2)
    void createBookingsOverlap() throws Exception {
        BookingCreateDTO bookingCreateDTO = BookingCreateDTO.builder().
                hotelId(1L).
                checkInDate(LocalDate.of(2023, 2, 21)).
                checkOutDate(LocalDate.of(2023, 3, 1)).
                numberGuests(5).
                roomIds(List.of(2L)).
                serviceIds(List.of(1L, 3L)).
                build();
        mvc.perform(MockMvcRequestBuilders.post("/bookings").
                        content(toJson(mapper, bookingCreateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + secondCustomerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isBadRequest());
        bookingCreateDTO = BookingCreateDTO.builder().
                hotelId(1L).
                checkInDate(LocalDate.of(2023, 3, 11)).
                checkOutDate(LocalDate.of(2023, 3, 15)).
                numberGuests(5).
                roomIds(List.of(2L)).
                serviceIds(List.of(1L, 3L)).
                build();
        mvc.perform(MockMvcRequestBuilders.post("/bookings").
                        content(toJson(mapper, bookingCreateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + secondCustomerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isBadRequest());
        bookingCreateDTO = BookingCreateDTO.builder().
                hotelId(1L).
                checkInDate(LocalDate.of(2023, 3, 2)).
                checkOutDate(LocalDate.of(2023, 3, 5)).
                numberGuests(5).
                roomIds(List.of(2L)).
                serviceIds(List.of(1L, 3L)).
                build();
        mvc.perform(MockMvcRequestBuilders.post("/bookings").
                        content(toJson(mapper, bookingCreateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + secondCustomerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isBadRequest());

    }

    @Test
    @Order(3)
    void createBookingsInsufficientRoomCapacity() throws Exception {
        BookingCreateDTO bookingCreateDTO = BookingCreateDTO.builder().
                hotelId(1L).
                checkInDate(LocalDate.of(2023, 12, 1)).
                checkOutDate(LocalDate.of(2023, 12, 12)).
                numberGuests(6).
                roomIds(List.of(3L)).
                serviceIds(List.of(1L, 3L)).
                build();
        mvc.perform(MockMvcRequestBuilders.post("/bookings").
                        content(toJson(mapper, bookingCreateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + secondCustomerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isBadRequest());
        bookingCreateDTO = BookingCreateDTO.builder().
                hotelId(1L).
                checkInDate(LocalDate.of(2023, 12, 1)).
                checkOutDate(LocalDate.of(2023, 12, 12)).
                numberGuests(8).
                roomIds(List.of(2L, 3L)).
                serviceIds(List.of(1L, 3L)).
                build();
        mvc.perform(MockMvcRequestBuilders.post("/bookings").
                        content(toJson(mapper, bookingCreateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + secondCustomerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    void createBookingWrongIds() throws Exception {
        BookingCreateDTO bookingCreateDTO = BookingCreateDTO.builder().
                hotelId(1L).
                checkInDate(LocalDate.of(2023, 12, 1)).
                checkOutDate(LocalDate.of(2023, 12, 12)).
                numberGuests(6).
                roomIds(List.of(4L, 5L)).
                serviceIds(List.of(1L, 3L)).
                build();
        mvc.perform(MockMvcRequestBuilders.post("/bookings").
                        content(toJson(mapper, bookingCreateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + secondCustomerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isBadRequest());
        bookingCreateDTO = BookingCreateDTO.builder().
                hotelId(2L).
                checkInDate(LocalDate.of(2023, 12, 1)).
                checkOutDate(LocalDate.of(2023, 12, 12)).
                numberGuests(6).
                roomIds(List.of(1L, 2L)).
                serviceIds(List.of(1L, 3L)).
                build();
        mvc.perform(MockMvcRequestBuilders.post("/bookings").
                        content(toJson(mapper, bookingCreateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + secondCustomerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isBadRequest());
        bookingCreateDTO = BookingCreateDTO.builder().
                hotelId(1L).
                checkInDate(LocalDate.of(2023, 12, 1)).
                checkOutDate(LocalDate.of(2023, 12, 12)).
                numberGuests(6).
                roomIds(List.of(1L, 2L)).
                serviceIds(List.of(4L, 5L)).
                build();
        mvc.perform(MockMvcRequestBuilders.post("/bookings").
                        content(toJson(mapper, bookingCreateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + secondCustomerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isBadRequest());
        bookingCreateDTO = BookingCreateDTO.builder().
                hotelId(2L).
                checkInDate(LocalDate.of(2023, 12, 1)).
                checkOutDate(LocalDate.of(2023, 12, 12)).
                numberGuests(6).
                roomIds(List.of(4L, 5L)).
                serviceIds(List.of(1L, 2L)).
                build();
        mvc.perform(MockMvcRequestBuilders.post("/bookings").
                        content(toJson(mapper, bookingCreateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + secondCustomerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    void listBookings() throws Exception {
        //Test with user ID

        BookingQueryDTO queryDTO = BookingQueryDTO.builder().userId(3L).build();
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/bookings").
                        content(toJson(mapper, queryDTO)).
                        characterEncoding(StandardCharsets.UTF_8).
                        contentType(MediaType.APPLICATION_JSON).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        List<BookingDTO> bookingDTOList = listFromJson(mapper, result, BookingDTO[].class);
        assertEquals(3, bookingDTOList.size());


        queryDTO = BookingQueryDTO.builder().userId(4L).build();
        result = mvc.perform(MockMvcRequestBuilders.get("/bookings").
                        content(toJson(mapper, queryDTO)).
                        characterEncoding(StandardCharsets.UTF_8).
                        contentType(MediaType.APPLICATION_JSON).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        bookingDTOList = listFromJson(mapper, result, BookingDTO[].class);
        assertTrue(bookingDTOList.stream().map(BookingDTO::getId).toList().containsAll(List.of(4L, 5L, 6L)));
        assertEquals(3, bookingDTOList.size());

        queryDTO = BookingQueryDTO.builder().userId(5L).build();
        result = mvc.perform(MockMvcRequestBuilders.get("/bookings").
                        content(toJson(mapper, queryDTO)).
                        characterEncoding(StandardCharsets.UTF_8).
                        contentType(MediaType.APPLICATION_JSON).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        bookingDTOList = listFromJson(mapper, result, BookingDTO[].class);
        assertEquals(2, bookingDTOList.size());

        //Test with user ID and Hotel ID
        queryDTO = BookingQueryDTO.builder().userId(4L).hotelId(1L).build();
        result = mvc.perform(MockMvcRequestBuilders.get("/bookings").
                        content(toJson(mapper, queryDTO)).
                        characterEncoding(StandardCharsets.UTF_8).
                        contentType(MediaType.APPLICATION_JSON).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        bookingDTOList = listFromJson(mapper, result, BookingDTO[].class);
        assertEquals(1, bookingDTOList.size());
        assertTrue(bookingDTOList.stream().map(BookingDTO::getId).toList().contains(4L));
        queryDTO = BookingQueryDTO.builder().
                userId(4L).
                hotelId(2L).
                build();
        result = mvc.perform(MockMvcRequestBuilders.get("/bookings").
                        content(toJson(mapper, queryDTO)).
                        characterEncoding(StandardCharsets.UTF_8).
                        contentType(MediaType.APPLICATION_JSON).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        bookingDTOList = listFromJson(mapper, result, BookingDTO[].class);
        assertEquals(2, bookingDTOList.size());
        assertTrue(bookingDTOList.stream().map(BookingDTO::getId).toList().containsAll(List.of(5L, 6L)));
        //Test with user ID, Hotel ID and Check-in/out dates
        queryDTO = BookingQueryDTO.builder().
                userId(4L).
                hotelId(2L).
                checkInDate(LocalDate.of(2023, 3, 19)).
                checkOutDate(LocalDate.of(2023, 3, 28)).
                build();
        result = mvc.perform(MockMvcRequestBuilders.get("/bookings").
                        content(toJson(mapper, queryDTO)).
                        characterEncoding(StandardCharsets.UTF_8).
                        contentType(MediaType.APPLICATION_JSON).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        bookingDTOList = listFromJson(mapper, result, BookingDTO[].class);
        assertEquals(1, bookingDTOList.size());
        assertTrue(bookingDTOList.stream().map(BookingDTO::getId).toList().contains(5L));
        //Assert that Customers can't use this endpoint
        mvc.perform(MockMvcRequestBuilders.get("/bookings").
                        content(toJson(mapper, queryDTO)).
                        characterEncoding(StandardCharsets.UTF_8).
                        contentType(MediaType.APPLICATION_JSON).
                        header("Authorization", "Bearer " + firstCustomerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isForbidden());
    }

    @Test
    @Order(6)
    void updateBookings() throws Exception {
        //Assert Overlap in Updates
        //Overlaps with Booking #2
        BookingUpdateDTO bookingUpdateDTO = BookingUpdateDTO.builder().
                checkInDate(LocalDate.of(2023, 3, 6)).
                checkOutDate(LocalDate.of(2023, 3, 10)).
                numberGuests(5).
                roomIds(List.of(1L)).
                serviceIds(List.of()).
                bookingStatus(BookingStatus.PENDING).build();
        mvc.perform(MockMvcRequestBuilders.put("/bookings/1").
                        content(toJson(mapper, bookingUpdateDTO)).
                        characterEncoding(StandardCharsets.UTF_8).
                        contentType(MediaType.APPLICATION_JSON).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isBadRequest());
        bookingUpdateDTO = BookingUpdateDTO.builder().
                checkInDate(LocalDate.of(2023, 7, 10)).
                checkOutDate(LocalDate.of(2023, 7, 20)).
                numberGuests(4).
                roomIds(List.of(5L)).
                serviceIds(List.of()).
                bookingStatus(BookingStatus.PENDING).build();
        mvc.perform(MockMvcRequestBuilders.put("/bookings/8").
                        content(toJson(mapper, bookingUpdateDTO)).
                        characterEncoding(StandardCharsets.UTF_8).
                        contentType(MediaType.APPLICATION_JSON).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());
        //Assert that status cannot be changed by Customer except from Pending to Cancelled
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/bookings/6").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        BookingDTO bookingDTO = fromJson(mapper, result, BookingDTO.class);
        //Assert that customer can't change the status from cancelled
        bookingUpdateDTO = BookingUpdateDTO.builder().
                checkInDate(bookingDTO.getCheckInDate()).
                checkOutDate(bookingDTO.getCheckOutDate()).
                numberGuests(bookingDTO.getNumberGuests()).
                roomIds(bookingDTO.getRooms().stream().map(RoomDTO::getId).toList()).
                serviceIds(bookingDTO.getServices().stream().map(ServiceHotelDTO::getId).toList()).
                bookingStatus(BookingStatus.CANCELLED).build();
        mvc.perform(MockMvcRequestBuilders.put("/bookings/6").
                        content(toJson(mapper, bookingUpdateDTO)).
                        characterEncoding(StandardCharsets.UTF_8).
                        contentType(MediaType.APPLICATION_JSON).
                        header("Authorization", "Bearer " + secondCustomerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());
        bookingUpdateDTO = BookingUpdateDTO.builder().
                checkInDate(bookingDTO.getCheckInDate()).
                checkOutDate(bookingDTO.getCheckOutDate()).
                numberGuests(bookingDTO.getNumberGuests()).
                roomIds(bookingDTO.getRooms().stream().map(RoomDTO::getId).toList()).
                serviceIds(bookingDTO.getServices().stream().map(ServiceHotelDTO::getId).toList()).
                bookingStatus(BookingStatus.PENDING).build();
        mvc.perform(MockMvcRequestBuilders.put("/bookings/6").
                        content(toJson(mapper, bookingUpdateDTO)).
                        characterEncoding(StandardCharsets.UTF_8).
                        contentType(MediaType.APPLICATION_JSON).
                        header("Authorization", "Bearer " + secondCustomerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isBadRequest());
        //Admin will update the booking status to completed
        bookingUpdateDTO = BookingUpdateDTO.builder().
                checkInDate(bookingDTO.getCheckInDate()).
                checkOutDate(bookingDTO.getCheckOutDate()).
                numberGuests(bookingDTO.getNumberGuests()).
                roomIds(bookingDTO.getRooms().stream().map(RoomDTO::getId).toList()).
                serviceIds(bookingDTO.getServices().stream().map(ServiceHotelDTO::getId).toList()).
                bookingStatus(BookingStatus.COMPLETED).build();
        mvc.perform(MockMvcRequestBuilders.put("/bookings/6").
                        content(toJson(mapper, bookingUpdateDTO)).
                        characterEncoding(StandardCharsets.UTF_8).
                        contentType(MediaType.APPLICATION_JSON).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());
        //Assert that customer can't downgrade the status from completed
        bookingUpdateDTO = BookingUpdateDTO.builder().
                checkInDate(bookingDTO.getCheckInDate()).
                checkOutDate(bookingDTO.getCheckOutDate()).
                numberGuests(bookingDTO.getNumberGuests()).
                roomIds(bookingDTO.getRooms().stream().map(RoomDTO::getId).toList()).
                serviceIds(bookingDTO.getServices().stream().map(ServiceHotelDTO::getId).toList()).
                bookingStatus(BookingStatus.PENDING).build();
        mvc.perform(MockMvcRequestBuilders.put("/bookings/6").
                        content(toJson(mapper, bookingUpdateDTO)).
                        characterEncoding(StandardCharsets.UTF_8).
                        contentType(MediaType.APPLICATION_JSON).
                        header("Authorization", "Bearer " + secondCustomerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isBadRequest());
        //If a User's role is Customer, it only can manage its bookings
        mvc.perform(MockMvcRequestBuilders.put("/bookings/6").
                        content(toJson(mapper, bookingUpdateDTO)).
                        characterEncoding(StandardCharsets.UTF_8).
                        contentType(MediaType.APPLICATION_JSON).
                        header("Authorization", "Bearer " + firstCustomerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isNotFound());
        //Manually update price as an employee
        BigDecimal currentPrice = bookingDTO.getPrice();
        BigDecimal price = BigDecimal.valueOf(888.88);
        BigDecimal price1 = BigDecimal.valueOf(999.99);
        bookingUpdateDTO = BookingUpdateDTO.builder().
                checkInDate(bookingDTO.getCheckInDate()).
                checkOutDate(bookingDTO.getCheckOutDate()).
                numberGuests(bookingDTO.getNumberGuests()).
                roomIds(bookingDTO.getRooms().stream().map(RoomDTO::getId).toList()).
                serviceIds(bookingDTO.getServices().stream().map(ServiceHotelDTO::getId).toList()).
                price(price).
                bookingStatus(BookingStatus.PENDING).
                build();
        result = mvc.perform(MockMvcRequestBuilders.put("/bookings/6").
                        content(toJson(mapper, bookingUpdateDTO)).
                        characterEncoding(StandardCharsets.UTF_8).
                        contentType(MediaType.APPLICATION_JSON).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        bookingDTO = fromJson(mapper, result, BookingDTO.class);
        assertEquals(price, bookingDTO.getPrice());
        //Assert price isn't changed as a Customer
        bookingUpdateDTO = BookingUpdateDTO.builder().
                checkInDate(bookingDTO.getCheckInDate()).
                checkOutDate(bookingDTO.getCheckOutDate()).
                numberGuests(bookingDTO.getNumberGuests()).
                roomIds(bookingDTO.getRooms().stream().map(RoomDTO::getId).toList()).
                serviceIds(bookingDTO.getServices().stream().map(ServiceHotelDTO::getId).toList()).
                price(price1).
                bookingStatus(BookingStatus.PENDING).
                build();
        result = mvc.perform(MockMvcRequestBuilders.put("/bookings/6").
                        content(toJson(mapper, bookingUpdateDTO)).
                        characterEncoding(StandardCharsets.UTF_8).
                        contentType(MediaType.APPLICATION_JSON).
                        header("Authorization", "Bearer " + secondCustomerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        bookingDTO = fromJson(mapper, result, BookingDTO.class);
        assertNotEquals(price1, bookingDTO.getPrice());
        assertEquals(currentPrice, bookingDTO.getPrice());

    }

    @Test
    @Order(7)
    void availableRooms() throws Exception {
        BookingAvailableRoomsDTO availableRoomsDTO = BookingAvailableRoomsDTO.builder().
                hotelId(1L).
                checkInDate(LocalDate.of(2023, 4, 20)).
                checkOutDate(LocalDate.of(2023, 5, 5)).build();
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/bookings/available-rooms").
                        content(toJson(mapper, availableRoomsDTO)).
                        characterEncoding(StandardCharsets.UTF_8).
                        contentType(MediaType.APPLICATION_JSON).
                        header("Authorization", "Bearer " + secondCustomerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        List<RoomDTO> roomDTOList = listFromJson(mapper, result, RoomDTO[].class);
        assertEquals(2, roomDTOList.size());
        assertTrue(roomDTOList.stream().map(RoomDTO::getId).toList().containsAll(List.of(1L, 3L)));
        availableRoomsDTO = BookingAvailableRoomsDTO.builder().
                hotelId(1L).
                checkInDate(LocalDate.of(2023, 5, 10)).
                checkOutDate(LocalDate.of(2023, 5, 20)).build();
        result = mvc.perform(MockMvcRequestBuilders.get("/bookings/available-rooms").
                        content(toJson(mapper, availableRoomsDTO)).
                        characterEncoding(StandardCharsets.UTF_8).
                        contentType(MediaType.APPLICATION_JSON).
                        header("Authorization", "Bearer " + secondCustomerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        roomDTOList = listFromJson(mapper, result, RoomDTO[].class);
        assertEquals(3, roomDTOList.size());
        assertTrue(roomDTOList.stream().map(RoomDTO::getId).toList().containsAll(List.of(1L, 2L, 3L)));
        availableRoomsDTO = BookingAvailableRoomsDTO.builder().
                hotelId(1L).
                checkInDate(LocalDate.of(2023, 3, 1)).
                checkOutDate(LocalDate.of(2023, 3, 5)).build();
        result = mvc.perform(MockMvcRequestBuilders.get("/bookings/available-rooms").
                        content(toJson(mapper, availableRoomsDTO)).
                        characterEncoding(StandardCharsets.UTF_8).
                        contentType(MediaType.APPLICATION_JSON).
                        header("Authorization", "Bearer " + secondCustomerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        roomDTOList = listFromJson(mapper, result, RoomDTO[].class);
        assertEquals(0, roomDTOList.size());
        availableRoomsDTO = BookingAvailableRoomsDTO.builder().
                hotelId(1L).
                checkInDate(LocalDate.of(2023, 3, 4)).
                checkOutDate(LocalDate.of(2023, 3, 7)).build();
        result = mvc.perform(MockMvcRequestBuilders.get("/bookings/available-rooms").
                        content(toJson(mapper, availableRoomsDTO)).
                        characterEncoding(StandardCharsets.UTF_8).
                        contentType(MediaType.APPLICATION_JSON).
                        header("Authorization", "Bearer " + secondCustomerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        roomDTOList = listFromJson(mapper, result, RoomDTO[].class);
        assertEquals(1, roomDTOList.size());
        assertTrue(roomDTOList.stream().map(RoomDTO::getId).toList().contains(1L));
    }

    @Test
    @Order(8)
    void bookingStatistics() throws Exception {
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/bookings/statistics").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        BookingStatistics statistics = fromJson(mapper, result, BookingStatistics.class);
        assertFalse(statistics.getBookingDateStatistics().isEmpty());
        assertFalse(statistics.getCheckInDateStatistics().isEmpty());
        List<IBookingStatistic> checkInDateStatistics = statistics.getCheckInDateStatistics();
        assertTrue(checkInDateStatistics.stream().anyMatch(
                statistic -> statistic.getHotelId() == 1 && statistic.getYear() == 2023 && statistic.getMonth() == 2 && statistic.getBookingCount() == 1)
        );
        assertTrue(checkInDateStatistics.stream().anyMatch(
                statistic -> statistic.getHotelId() == 1 && statistic.getYear() == 2023 && statistic.getMonth() == 3 && statistic.getBookingCount() == 2)
        );
        assertTrue(checkInDateStatistics.stream().anyMatch(
                statistic -> statistic.getHotelId() == 1 && statistic.getYear() == 2023 && statistic.getMonth() == 5 && statistic.getBookingCount() == 1)
        );
        assertTrue(checkInDateStatistics.stream().anyMatch(
                statistic -> statistic.getHotelId() == 2 && statistic.getYear() == 2023 && statistic.getMonth() == 3 && statistic.getBookingCount() == 3)
        );
        assertTrue(checkInDateStatistics.stream().anyMatch(
                statistic -> statistic.getHotelId() == 2 && statistic.getYear() == 2023 && statistic.getMonth() == 7 && statistic.getBookingCount() == 1)
        );
        mvc.perform(MockMvcRequestBuilders.get("/bookings/statistics").
                        header("Authorization", "Bearer " + firstCustomerToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isForbidden());
    }


    @Test
    @Order(9)
    void getBookings() throws Exception {
        /*
        Get bookings on the /bookings and /bookings/{id} endpoint and on each customer's profile,
        either by the users/{id} endpoint or the users/self endpoint
         */
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/bookings/2").
                        header("Authorization", "Bearer " + firstCustomerToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        BookingDTO bookingDTO = fromJson(mapper, result, BookingDTO.class);
        assertEquals(1, bookingDTO.getHotelId());
        assertEquals(3, bookingDTO.getUserId());
        assertEquals(LocalDate.of(2023, 3, 7), bookingDTO.getCheckInDate());
        assertEquals(LocalDate.of(2023, 3, 12), bookingDTO.getCheckOutDate());
        assertTrue(bookingDTO.getRooms().stream().map(RoomDTO::getId).toList().contains(1L));
        BookingQueryDTO queryDTO = BookingQueryDTO.builder().build();
        result = mvc.perform(MockMvcRequestBuilders.get("/bookings").
                        content(toJson(mapper, queryDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        List<BookingDTO> bookingDTOList = listFromJson(mapper, result, BookingDTO[].class);
        bookingDTO = bookingDTOList.stream().filter(booking -> booking.getId() == 2).findFirst().orElseThrow();
        assertEquals(1, bookingDTO.getHotelId());
        assertEquals(3, bookingDTO.getUserId());
        assertEquals(LocalDate.of(2023, 3, 7), bookingDTO.getCheckInDate());
        assertEquals(LocalDate.of(2023, 3, 12), bookingDTO.getCheckOutDate());
        assertTrue(bookingDTO.getRooms().stream().map(RoomDTO::getId).toList().contains(1L));
        result = mvc.perform(MockMvcRequestBuilders.get("/bookings/2").
                        header("Authorization", "Bearer " + employeeToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        bookingDTO = fromJson(mapper, result, BookingDTO.class);
        assertEquals(1, bookingDTO.getHotelId());
        assertEquals(3, bookingDTO.getUserId());
        assertEquals(LocalDate.of(2023, 3, 7), bookingDTO.getCheckInDate());
        assertEquals(LocalDate.of(2023, 3, 12), bookingDTO.getCheckOutDate());
        assertTrue(bookingDTO.getRooms().stream().map(RoomDTO::getId).toList().contains(1L));
        result = mvc.perform(MockMvcRequestBuilders.get("/users/self").
                        header("Authorization", "Bearer " + firstCustomerToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        UserDTO dto = fromJson(mapper, result, UserDTO.class);
        bookingDTO = dto.getBookings().stream().filter(booking -> booking.getId() == 2).findFirst().orElseThrow();
        assertEquals(1, bookingDTO.getHotelId());
        assertEquals(3, bookingDTO.getUserId());
        assertEquals(LocalDate.of(2023, 3, 7), bookingDTO.getCheckInDate());
        assertEquals(LocalDate.of(2023, 3, 12), bookingDTO.getCheckOutDate());
        assertTrue(bookingDTO.getRooms().stream().map(RoomDTO::getId).toList().contains(1L));
        result = mvc.perform(MockMvcRequestBuilders.get("/users/3").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        UserDTOAdmin userDTOAdmin = fromJson(mapper, result, UserDTOAdmin.class);
        bookingDTO = userDTOAdmin.getBookings().stream().filter(booking -> booking.getId() == 2).findFirst().orElseThrow();
        assertEquals(1, bookingDTO.getHotelId());
        assertEquals(3, bookingDTO.getUserId());
        assertEquals(LocalDate.of(2023, 3, 7), bookingDTO.getCheckInDate());
        assertEquals(LocalDate.of(2023, 3, 12), bookingDTO.getCheckOutDate());
        assertTrue(bookingDTO.getRooms().stream().map(RoomDTO::getId).toList().contains(1L));
        //Assert that Customers can't access bookings that aren't booked by them and customers/admins can access any booking
        mvc.perform(MockMvcRequestBuilders.get("/bookings/4").
                        header("Authorization", "Bearer " + firstCustomerToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isNotFound());
        mvc.perform(MockMvcRequestBuilders.get("/bookings/1").
                        header("Authorization", "Bearer " + firstCustomerToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());
        mvc.perform(MockMvcRequestBuilders.get("/bookings/4").
                        header("Authorization", "Bearer " + employeeToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());
        mvc.perform(MockMvcRequestBuilders.get("/bookings/4").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());
    }

    @Test
    @Order(10)
    void deleteBookings() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/bookings/2").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());

        BookingQueryDTO queryDTO = BookingQueryDTO.builder().build();
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/bookings").
                        content(toJson(mapper, queryDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        List<BookingDTO> bookingDTOList = listFromJson(mapper, result, BookingDTO[].class);
        assertTrue(bookingDTOList.stream().anyMatch(bookingDTO -> bookingDTO.getId() == 2));
        mvc.perform(MockMvcRequestBuilders.delete("/bookings/2").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());
        mvc.perform(MockMvcRequestBuilders.get("/bookings/2").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isNotFound());
        result = mvc.perform(MockMvcRequestBuilders.get("/bookings").
                        content(toJson(mapper, queryDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        bookingDTOList = listFromJson(mapper, result, BookingDTO[].class);
        assertFalse(bookingDTOList.stream().anyMatch(bookingDTO -> bookingDTO.getId() == 2));
    }

}
