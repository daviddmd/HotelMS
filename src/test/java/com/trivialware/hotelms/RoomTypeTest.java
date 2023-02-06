package com.trivialware.hotelms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trivialware.hotelms.Models.RoomType.RoomTypeCreateDTO;
import com.trivialware.hotelms.Models.RoomType.RoomTypeDTO;
import com.trivialware.hotelms.Models.RoomType.RoomTypeUpdateDTO;
import com.trivialware.hotelms.Models.User.UserLoginDTO;
import com.trivialware.hotelms.Models.User.UserRegisterDTO;
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
import java.math.RoundingMode;
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
public class RoomTypeTest {
    ObjectMapper mapper;
    String adminToken, customerToken;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserService userService;

    @BeforeAll
    void setup() throws Exception {
        UserRegisterDTO adminRegisterDTO, firstCustomerRegisterDTO;
        adminRegisterDTO = UserRegisterDTO.builder().first_name(firstNameAdmin).last_name(lastNameAdmin).username(usernameAdmin).email(emailAdmin).password(passwordAdmin).phoneNumber(phoneNumberAdmin).build();
        firstCustomerRegisterDTO = UserRegisterDTO.builder().first_name(firstNameFirstCustomer).last_name(lastNameFirstCustomer).username(usernameFirstCustomer).email(emailFirstCustomer).password(passwordFirstCustomer).phoneNumber(phoneNumberFirstCustomer).build();
        userService.register(adminRegisterDTO);
        userService.register(firstCustomerRegisterDTO);
        adminToken = userService.login(UserLoginDTO.builder().usernameOrEmail(emailAdmin).password(passwordAdmin).rememberMe(true).build());
        customerToken = userService.login(UserLoginDTO.builder().usernameOrEmail(emailFirstCustomer).password(passwordFirstCustomer).rememberMe(true).build());
        mapper = new ObjectMapper();
        List<RoomTypeCreateDTO> roomTypeCreateDTOList = List.of(
                RoomTypeCreateDTO.builder().name("2 King-Sized Bed Suite").maximumCapacity(4).basePrice(new BigDecimal(40)).build(),
                RoomTypeCreateDTO.builder().name("3 King-Sized Bed Suite").maximumCapacity(6).basePrice(new BigDecimal(80)).build(),
                RoomTypeCreateDTO.builder().name("2 Single Bed Suite").maximumCapacity(2).basePrice(new BigDecimal(20)).build()
        );
        for (RoomTypeCreateDTO dto : roomTypeCreateDTOList) {
            mvc.perform(MockMvcRequestBuilders.post("/room-types").
                    content(toJson(mapper, dto)).
                    characterEncoding(StandardCharsets.UTF_8).header("Authorization", "Bearer " + adminToken).
                    contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        }
    }

    @Test
    @Order(1)
    void listRoomTypes() throws Exception {
        //Get list of Room Types as an Admin
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/room-types").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        List<RoomTypeDTO> roomTypeDTOS = listFromJson(mapper, result, RoomTypeDTO[].class);
        assertEquals(3, roomTypeDTOS.size());
        assertTrue(roomTypeDTOS.stream().anyMatch(roomTypeDTO -> roomTypeDTO.getName().equals("2 Single Bed Suite")));
        assertTrue(roomTypeDTOS.stream().anyMatch(roomTypeDTO -> roomTypeDTO.getName().equals("2 King-Sized Bed Suite")));
        assertTrue(roomTypeDTOS.stream().anyMatch(roomTypeDTO -> roomTypeDTO.getName().equals("3 King-Sized Bed Suite")));
        //Assert that customers can't use this endpoint
        assertDoesNotThrow(() -> mvc.perform(MockMvcRequestBuilders.get("/room-types").
                        header("Authorization", "Bearer " + customerToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isForbidden()));
    }

    @Test
    @Order(2)
    void createRoomType() throws Exception {
        String roomTypeName = "3 2-Floor Bunk-Beds";
        int roomTypeMaximumCapacity = 6;
        BigDecimal roomTypeBasePrice = BigDecimal.valueOf(50.39);
        //Assert current number of rooms
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/room-types").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        List<RoomTypeDTO> roomTypeDTOS = listFromJson(mapper, result, RoomTypeDTO[].class);
        assertEquals(3, roomTypeDTOS.size());
        //Instantiate builder object and create room type
        RoomTypeCreateDTO dto = RoomTypeCreateDTO.builder().name(roomTypeName).maximumCapacity(roomTypeMaximumCapacity).basePrice(roomTypeBasePrice).build();
        result = mvc.perform(MockMvcRequestBuilders.post("/room-types").
                        content(toJson(mapper, dto)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).
                andReturn();
        //Verify correctness of returned object
        RoomTypeDTO roomTypeDTO = fromJson(mapper, result, RoomTypeDTO.class);
        assertEquals(roomTypeName, roomTypeDTO.getName());
        assertEquals(4, roomTypeDTO.getId());
        assertEquals(roomTypeBasePrice, roomTypeDTO.getBasePrice());
        assertEquals(roomTypeMaximumCapacity, roomTypeDTO.getMaximumCapacity());
        //Fetch the room type object by its ID and verify its correctness
        result = mvc.perform(MockMvcRequestBuilders.get("/room-types/4").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        roomTypeDTO = fromJson(mapper, result, RoomTypeDTO.class);
        assertEquals(roomTypeName, roomTypeDTO.getName());
        assertEquals(4, roomTypeDTO.getId());
        assertEquals(roomTypeBasePrice, roomTypeDTO.getBasePrice());
        assertEquals(roomTypeMaximumCapacity, roomTypeDTO.getMaximumCapacity());
        //Verify that the room type object is present in the room types list
        result = mvc.perform(MockMvcRequestBuilders.get("/room-types").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        roomTypeDTOS = listFromJson(mapper, result, RoomTypeDTO[].class);
        assertEquals(4, roomTypeDTOS.size());
        assertTrue(roomTypeDTOS.stream().anyMatch(rt -> rt.getName().equals(roomTypeName)));
        //Assert that customers can't use this endpoint
        mvc.perform(MockMvcRequestBuilders.post("/room-types").
                        content(toJson(mapper, dto)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + customerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isForbidden());
        //Assert that new room types with the name of existent room types can't be created
        dto = RoomTypeCreateDTO.builder().name(roomTypeName).maximumCapacity(100).basePrice(BigDecimal.valueOf(20)).build();
        mvc.perform(MockMvcRequestBuilders.post("/room-types").
                        content(toJson(mapper, dto)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isBadRequest());
    }

    @Test
    @Order(3)
    void updateRoomType() throws Exception {
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/room-types/1").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        RoomTypeDTO dto = fromJson(mapper, result, RoomTypeDTO.class);
        assertEquals("2 King-Sized Bed Suite", dto.getName());
        assertEquals(4, dto.getMaximumCapacity());
        assertEquals(BigDecimal.valueOf(40.00).setScale(2, RoundingMode.HALF_UP), dto.getBasePrice());
        String newRoomTypeName = "Two and a Half Bed Bedroom";
        int newMaximumCapacity = 5;
        BigDecimal newBasePrice = BigDecimal.valueOf(1000000.42);
        RoomTypeUpdateDTO updateDTO = RoomTypeUpdateDTO.builder().name(newRoomTypeName).maximumCapacity(newMaximumCapacity).basePrice(newBasePrice).build();
        result = mvc.perform(MockMvcRequestBuilders.put("/room-types/1").
                        content(toJson(mapper, updateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        dto = fromJson(mapper, result, RoomTypeDTO.class);
        assertEquals(newRoomTypeName, dto.getName());
        assertEquals(newMaximumCapacity, dto.getMaximumCapacity());
        assertEquals(newBasePrice, dto.getBasePrice());
        result = mvc.perform(MockMvcRequestBuilders.get("/room-types/1").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        dto = fromJson(mapper, result, RoomTypeDTO.class);
        assertEquals(newRoomTypeName, dto.getName());
        assertEquals(newMaximumCapacity, dto.getMaximumCapacity());
        assertEquals(newBasePrice, dto.getBasePrice());
        //Assert that the update operation fails if the updated name is equal to an existent one
        updateDTO = RoomTypeUpdateDTO.builder().name("2 Single Bed Suite").maximumCapacity(newMaximumCapacity).basePrice(newBasePrice).build();
        mvc.perform(MockMvcRequestBuilders.put("/room-types/1").
                        content(toJson(mapper, updateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isBadRequest());
        //Assert that customers can't update room types
        mvc.perform(MockMvcRequestBuilders.put("/room-types/1").
                        content(toJson(mapper, updateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + customerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isForbidden());
    }

    @Test
    @Order(4)
    void deleteRoomType() throws Exception {
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/room-types/3").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        RoomTypeDTO dto = fromJson(mapper, result, RoomTypeDTO.class);
        assertEquals("2 Single Bed Suite", dto.getName());
        mvc.perform(MockMvcRequestBuilders.delete("/room-types/3").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());
        mvc.perform(MockMvcRequestBuilders.get("/room-types/3").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isNotFound());
        //Assert that customers can't delete existent or non-existent room types
        mvc.perform(MockMvcRequestBuilders.delete("/room-types/1").
                        header("Authorization", "Bearer " + customerToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isForbidden());
        mvc.perform(MockMvcRequestBuilders.delete("/room-types/3").
                        header("Authorization", "Bearer " + customerToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isForbidden());

    }

}
