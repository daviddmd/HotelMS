package com.trivialware.hotelms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trivialware.hotelms.Models.Hotel.*;
import com.trivialware.hotelms.Models.Room.RoomCreateDTO;
import com.trivialware.hotelms.Models.Room.RoomDTO;
import com.trivialware.hotelms.Models.Room.RoomDTOAdmin;
import com.trivialware.hotelms.Models.Room.RoomUpdateDTO;
import com.trivialware.hotelms.Models.RoomType.RoomTypeCreateDTO;
import com.trivialware.hotelms.Models.User.UserLoginDTO;
import com.trivialware.hotelms.Models.User.UserRegisterDTO;
import com.trivialware.hotelms.Services.HotelService;
import com.trivialware.hotelms.Services.RoomTypeService;
import com.trivialware.hotelms.Services.UserService;
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
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.trivialware.hotelms.Helper.*;
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
public class RoomTest {
    ObjectMapper mapper;
    String adminToken, customerToken;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserService userService;
    @Autowired
    private RoomTypeService roomTypeService;
    @Autowired
    private HotelService hotelService;

    @BeforeAll
    void setup() throws Exception {
        userService.register(UserRegisterDTO.builder().first_name(firstNameAdmin).last_name(lastNameAdmin).username(usernameAdmin).email(emailAdmin).password(passwordAdmin).phoneNumber(phoneNumberAdmin).build());
        userService.register(UserRegisterDTO.builder().first_name(firstNameFirstCustomer).last_name(lastNameFirstCustomer).username(usernameFirstCustomer).email(emailFirstCustomer).password(passwordFirstCustomer).phoneNumber(phoneNumberFirstCustomer).build());
        adminToken = userService.login(UserLoginDTO.builder().usernameOrEmail(emailAdmin).password(passwordAdmin).rememberMe(true).build());
        customerToken = userService.login(UserLoginDTO.builder().usernameOrEmail(emailFirstCustomer).password(passwordFirstCustomer).rememberMe(true).build());
        mapper = new ObjectMapper();
        hotelService.addHotel(HotelCreateDTO.builder().name("Hotel 2").code("HOTEL2").description("Hotel 2 Description").build());
        hotelService.addHotel(HotelCreateDTO.builder().name("Hotel 3").code("HOTEL3").description("Hotel 3 Description").build());
        hotelService.addHotel(HotelCreateDTO.builder().name("Hotel 4").code("HOTEL4").description("Hotel 4 Description").build());
        roomTypeService.addRoomType(RoomTypeCreateDTO.builder().name("2 King-Sized Bed Suite").maximumCapacity(4).basePrice(new BigDecimal(40)).build());
        roomTypeService.addRoomType(RoomTypeCreateDTO.builder().name("3 King-Sized Bed Suite").maximumCapacity(6).basePrice(new BigDecimal(80)).build());
        roomTypeService.addRoomType(RoomTypeCreateDTO.builder().name("2 Single Bed Suite").maximumCapacity(2).basePrice(new BigDecimal(20)).build());
        List<RoomCreateDTO> roomCreateDTOList = List.of(
                RoomCreateDTO.builder().code("ROOM1").name("Room 1").description("Room 1 on Floor 1").roomTypeID(3L).price(new BigDecimal(50)).hotelID(1L).build(),
                RoomCreateDTO.builder().code("ROOM2").name("Room 2").description("Room 2 on Floor 1").roomTypeID(3L).price(new BigDecimal(50)).hotelID(1L).build(),
                RoomCreateDTO.builder().code("ROOM1").name("Room 1").description("Room 1 on Floor 1").roomTypeID(3L).price(new BigDecimal(50)).hotelID(2L).build(),
                RoomCreateDTO.builder().code("ROOM2").name("Room 2").description("Room 2 on Floor 1").roomTypeID(3L).price(new BigDecimal(50)).hotelID(2L).build()
        );
        for (RoomCreateDTO dto : roomCreateDTOList) {
            mvc.perform(MockMvcRequestBuilders.post("/rooms").
                    content(toJson(mapper, dto)).
                    characterEncoding(StandardCharsets.UTF_8).header("Authorization", "Bearer " + adminToken).
                    contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        }
    }

    @Test
    @Order(1)
    void listRooms() throws Exception {
        //List all rooms and check for room existence in Hotel (get by ID and number of rooms)
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/rooms").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        List<RoomDTOAdmin> roomDTOAdminList = listFromJson(mapper, result, RoomDTOAdmin[].class);
        assertEquals(4, roomDTOAdminList.size());
        assertTrue(roomDTOAdminList.stream().anyMatch(roomDTOAdmin -> roomDTOAdmin.getCode().equals("ROOM1") && roomDTOAdmin.getHotelId() == 1));
        assertTrue(roomDTOAdminList.stream().anyMatch(roomDTOAdmin -> roomDTOAdmin.getCode().equals("ROOM2") && roomDTOAdmin.getHotelId() == 1));
        assertTrue(roomDTOAdminList.stream().anyMatch(roomDTOAdmin -> roomDTOAdmin.getCode().equals("ROOM1") && roomDTOAdmin.getHotelId() == 2));
        assertTrue(roomDTOAdminList.stream().anyMatch(roomDTOAdmin -> roomDTOAdmin.getCode().equals("ROOM2") && roomDTOAdmin.getHotelId() == 2));
        result = mvc.perform(MockMvcRequestBuilders.get("/hotels").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        List<HotelDTOSimpleAdmin> hotelDTOSimpleList = listFromJson(mapper, result, HotelDTOSimpleAdmin[].class);
        assertEquals(2, hotelDTOSimpleList.stream().filter(hotelDTOSimpleAdmin -> hotelDTOSimpleAdmin.getId() == 1).findFirst().orElseThrow().getNumberRooms());
        assertEquals(2, hotelDTOSimpleList.stream().filter(hotelDTOSimpleAdmin -> hotelDTOSimpleAdmin.getId() == 2).findFirst().orElseThrow().getNumberRooms());
        result = mvc.perform(MockMvcRequestBuilders.get("/hotels/1").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        HotelDTOAdmin hotelDTOAdmin = fromJson(mapper, result, HotelDTOAdmin.class);
        assertTrue(hotelDTOAdmin.getRooms().stream().anyMatch(roomDTO -> roomDTO.getCode().equals("ROOM1")));
        assertTrue(hotelDTOAdmin.getRooms().stream().anyMatch(roomDTO -> roomDTO.getCode().equals("ROOM2")));
        //Assert that Customers can't list rooms
        mvc.perform(MockMvcRequestBuilders.get("/rooms").
                        header("Authorization", "Bearer " + customerToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isForbidden());

    }

    @Test
    @Order(2)
    void createRoom() throws Exception {
        String newRoomName = "Room 3";
        String newRoomCode = "ROOM3";
        String newRoomDescription = "Room 3 on Floor 1";
        long newRoomTypeId = 2L;
        BigDecimal newRoomPrice = BigDecimal.valueOf(53.59);
        RoomCreateDTO roomCreateDTO = RoomCreateDTO.builder().
                name(newRoomName).
                code(newRoomCode).
                description(newRoomDescription).
                roomTypeID(newRoomTypeId).
                price(newRoomPrice).
                hotelID(1L).
                build();
        MvcResult result = mvc.perform(MockMvcRequestBuilders.post("/rooms").
                        content(toJson(mapper, roomCreateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).
                andReturn();
        RoomDTOAdmin roomDTOAdmin = fromJson(mapper, result, RoomDTOAdmin.class);
        assertEquals(newRoomName, roomDTOAdmin.getName());
        assertEquals(newRoomDescription, roomDTOAdmin.getDescription());
        assertEquals(newRoomCode, roomDTOAdmin.getCode());
        assertEquals(5, roomDTOAdmin.getId());
        assertEquals("3 King-Sized Bed Suite", roomDTOAdmin.getRoomTypeName());
        assertEquals(newRoomPrice, roomDTOAdmin.getPrice());
        //Get Created Room by its ID
        result = mvc.perform(MockMvcRequestBuilders.get("/rooms/5").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        roomDTOAdmin = fromJson(mapper, result, RoomDTOAdmin.class);
        assertEquals(newRoomName, roomDTOAdmin.getName());
        assertEquals(newRoomDescription, roomDTOAdmin.getDescription());
        assertEquals(newRoomCode, roomDTOAdmin.getCode());
        assertEquals(5, roomDTOAdmin.getId());
        assertEquals("3 King-Sized Bed Suite", roomDTOAdmin.getRoomTypeName());
        assertEquals(newRoomPrice, roomDTOAdmin.getPrice());
        //Assert that room is present in the hotel
        result = mvc.perform(MockMvcRequestBuilders.get("/hotels/1").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        HotelDTOAdmin hotelDTOAdmin = fromJson(mapper, result, HotelDTOAdmin.class);
        assertTrue(hotelDTOAdmin.getRooms().stream().anyMatch(roomDTO -> roomDTO.getCode().equals(newRoomCode)));
        //Assert that creating a new room with the same code as an existent room in the same Hotel fails
        roomCreateDTO = RoomCreateDTO.builder().
                name("New Room Name").
                code(newRoomCode).
                description("New Room Description").
                roomTypeID(newRoomTypeId).
                price(newRoomPrice).
                hotelID(1L).
                build();
        mvc.perform(MockMvcRequestBuilders.post("/rooms").
                        content(toJson(mapper, roomCreateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isBadRequest());
        //Expect the creation to succeed when creating on a Hotel (2) that doesn't have a room with an existent ID (ROOM3)
        roomCreateDTO = RoomCreateDTO.builder().
                name(newRoomName).
                code(newRoomCode).
                description(newRoomDescription).
                roomTypeID(newRoomTypeId).
                price(newRoomPrice).
                hotelID(2L).
                build();
        result = mvc.perform(MockMvcRequestBuilders.post("/rooms").
                        content(toJson(mapper, roomCreateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        roomDTOAdmin = fromJson(mapper, result, RoomDTOAdmin.class);
        assertEquals(6, roomDTOAdmin.getId());
        //Assert that Customers can't create rooms
        mvc.perform(MockMvcRequestBuilders.post("/rooms").
                        content(toJson(mapper, roomCreateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + customerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isForbidden());
    }

    @Test
    @Order(3)
    void updateRoom() throws Exception {
        String newRoomName = "New Room 2";
        String newRoomCode = "NEWROOM2";
        String newRoomDescription = "New Room 2 Description";
        long newRoomTypeId = 1L;
        BigDecimal newRoomPrice = BigDecimal.valueOf(42.42);
        RoomUpdateDTO roomUpdateDTO = RoomUpdateDTO.builder().
                name(newRoomName).
                code(newRoomCode).
                description(newRoomDescription).
                roomTypeID(newRoomTypeId).
                price(newRoomPrice).
                active(true).
                build();
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/rooms/2").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        RoomDTOAdmin roomDTOAdmin = fromJson(mapper, result, RoomDTOAdmin.class);
        assertEquals("ROOM2", roomDTOAdmin.getCode());
        result = mvc.perform(MockMvcRequestBuilders.put("/rooms/2").
                        content(toJson(mapper, roomUpdateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).
                andReturn();
        roomDTOAdmin = fromJson(mapper, result, RoomDTOAdmin.class);
        assertEquals(newRoomName, roomDTOAdmin.getName());
        assertEquals(newRoomCode, roomDTOAdmin.getCode());
        assertEquals("2 King-Sized Bed Suite", roomDTOAdmin.getRoomTypeName());
        assertEquals(4, roomDTOAdmin.getRoomTypeCapacity());
        assertEquals(newRoomDescription, roomDTOAdmin.getDescription());
        assertEquals(newRoomPrice, roomDTOAdmin.getPrice());
        assertEquals(1, roomDTOAdmin.getHotelId());
        result = mvc.perform(MockMvcRequestBuilders.get("/hotels/1").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        HotelDTOAdmin hotelDTOAdmin = fromJson(mapper, result, HotelDTOAdmin.class);
        assertTrue(hotelDTOAdmin.getRooms().stream().anyMatch(roomDTO -> roomDTO.getCode().equals(newRoomCode)));
        //Assert that a room code can't be updated to an existent one
        roomUpdateDTO = RoomUpdateDTO.builder().
                name(newRoomName).
                code("ROOM1").
                description(newRoomDescription).
                roomTypeID(newRoomTypeId).
                price(newRoomPrice).
                active(true).
                build();
        mvc.perform(MockMvcRequestBuilders.put("/rooms/2").
                        content(toJson(mapper, roomUpdateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isBadRequest());
        //Assert that a room can be updated with the same code
        roomUpdateDTO = RoomUpdateDTO.builder().
                name("Random room name").
                code(newRoomCode).
                description("Random room description").
                roomTypeID(newRoomTypeId).
                price(newRoomPrice).
                active(true).
                build();
        mvc.perform(MockMvcRequestBuilders.put("/rooms/2").
                        content(toJson(mapper, roomUpdateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());
        //Assert that customers can't update rooms
        mvc.perform(MockMvcRequestBuilders.put("/rooms/2").
                        content(toJson(mapper, roomUpdateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + customerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isForbidden());
    }

    @Test
    @Order(4)
    void deleteRoom() throws Exception {
        //Assert that the room exists
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/rooms/2").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        RoomDTOAdmin roomDTOAdmin = fromJson(mapper, result, RoomDTOAdmin.class);
        assertEquals("NEWROOM2", roomDTOAdmin.getCode());
        assertEquals(1, roomDTOAdmin.getHotelId());
        //Assert that the room is present in Hotel 1
        result = mvc.perform(MockMvcRequestBuilders.get("/hotels/1").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        HotelDTOAdmin hotelDTOAdmin = fromJson(mapper, result, HotelDTOAdmin.class);
        assertTrue(hotelDTOAdmin.getRooms().stream().anyMatch(roomDTO -> roomDTO.getCode().equals(roomDTOAdmin.getCode())));
        //Get the current number of rooms in Hotel 1
        result = mvc.perform(MockMvcRequestBuilders.get("/hotels").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        List<HotelDTOSimpleAdmin> hotelDTOSimpleList = listFromJson(mapper, result, HotelDTOSimpleAdmin[].class);
        int numberRooms = hotelDTOSimpleList.stream().filter(h -> h.getId() == 1).findFirst().orElseThrow().getNumberRooms();
        //Delete the room
        mvc.perform(MockMvcRequestBuilders.delete("/rooms/2").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());
        //Check for existence of room in rooms endpoint; Assert that the room has been deleted
        mvc.perform(MockMvcRequestBuilders.get("/rooms/2").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isNotFound());
        //Assert that the number of rooms in Hotel 1 has decreased
        result = mvc.perform(MockMvcRequestBuilders.get("/hotels").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        hotelDTOSimpleList = listFromJson(mapper, result, HotelDTOSimpleAdmin[].class);
        assertEquals(numberRooms - 1, hotelDTOSimpleList.stream().filter(h -> h.getId() == 1).findFirst().orElseThrow().getNumberRooms());
        //Assert that the room isn't contained in Hotel 1
        result = mvc.perform(MockMvcRequestBuilders.get("/hotels/1").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        hotelDTOAdmin = fromJson(mapper, result, HotelDTOAdmin.class);
        assertFalse(hotelDTOAdmin.getRooms().stream().anyMatch(roomDTO -> roomDTO.getCode().equals(roomDTOAdmin.getCode())));
        //Assert that Customers can't delete rooms
        mvc.perform(MockMvcRequestBuilders.delete("/rooms/2").
                        header("Authorization", "Bearer " + customerToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isForbidden());
    }

    @Test
    @Order(5)
    void getDisabledHotelRoom() throws Exception {
        String newHotelName = "Hotel 2 Name";
        String newHotelDescription = "New Hotel Description";
        String newHotelCode = "HOTEL2NEW";
        HotelUpdateDTO dto = HotelUpdateDTO.builder().name(newHotelName).description(newHotelDescription).code(newHotelCode).active(false).build();
        MvcResult result = mvc.perform(MockMvcRequestBuilders.put("/hotels/2").
                        content(toJson(mapper, dto)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        HotelDTOAdmin hotelDTOAdmin = fromJson(mapper, result, HotelDTOAdmin.class);
        assertEquals("HOTEL2NEW", hotelDTOAdmin.getCode());
        assertFalse(hotelDTOAdmin.isActive());
        result = mvc.perform(MockMvcRequestBuilders.get("/rooms/3").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        RoomDTOAdmin roomDTOAdmin = fromJson(mapper, result, RoomDTOAdmin.class);
        assertEquals(2, roomDTOAdmin.getHotelId());
        mvc.perform(MockMvcRequestBuilders.get("/rooms/3").
                        header("Authorization", "Bearer " + customerToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isNotFound());

    }

    @Test
    @Order(6)
    void getDisabledRoom() throws Exception {
        //Get current room attributes and number of (available) rooms in the Hotel it's located in
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/rooms/1").
                        header("Authorization", "Bearer " + customerToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        RoomDTO roomDTO = fromJson(mapper, result, RoomDTO.class);
        assertEquals(1, roomDTO.getHotelId());
        result = mvc.perform(MockMvcRequestBuilders.get("/hotels/1").
                        header("Authorization", "Bearer " + customerToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        HotelDTO hotelDTO = fromJson(mapper, result, HotelDTO.class);
        int numberRooms = hotelDTO.getRooms().size();
        assertTrue(hotelDTO.getRooms().stream().anyMatch(room -> room.getCode().equals(roomDTO.getCode())));
        //Disable Room
        RoomUpdateDTO roomUpdateDTO = RoomUpdateDTO.builder().
                name(roomDTO.getName()).
                code(roomDTO.getCode()).
                description(roomDTO.getDescription()).
                roomTypeID(1L).
                price(roomDTO.getPrice()).
                active(false).
                build();
        result = mvc.perform(MockMvcRequestBuilders.put("/rooms/1").
                        content(toJson(mapper, roomUpdateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).
                andReturn();
        RoomDTOAdmin roomDTOAdmin = fromJson(mapper, result, RoomDTOAdmin.class);
        assertFalse(roomDTOAdmin.isActive());
        assertEquals(roomDTO.getCode(), roomDTOAdmin.getCode());
        //Assert that Room is accessible for Admin but not for Customer
        mvc.perform(MockMvcRequestBuilders.get("/rooms/1").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());
        mvc.perform(MockMvcRequestBuilders.get("/rooms/1").
                        header("Authorization", "Bearer " + customerToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isNotFound());
        //Assert that Number of Expected Rooms is decreased for Customer in Hotel Details
        result = mvc.perform(MockMvcRequestBuilders.get("/hotels/1").
                        header("Authorization", "Bearer " + customerToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        hotelDTO = fromJson(mapper, result, HotelDTO.class);
        assertEquals(numberRooms - 1, hotelDTO.getRooms().size());
        //Assert that Room isn't present in room list for Customer
        assertFalse(hotelDTO.getRooms().stream().anyMatch(room -> room.getCode().equals(roomDTO.getCode())));
        //Assert that number of rooms in Hotel listings is decreased
        result = mvc.perform(MockMvcRequestBuilders.get("/hotels").
                        header("Authorization", "Bearer " + customerToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        List<HotelDTOSimple> hotelDTOList = listFromJson(mapper, result, HotelDTOSimple[].class);
        assertEquals(numberRooms - 1, hotelDTOList.stream().filter(hotel -> hotel.getId() == 1).findFirst().orElseThrow().getNumberRooms());
    }
}
