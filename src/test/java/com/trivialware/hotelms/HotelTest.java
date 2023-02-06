package com.trivialware.hotelms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trivialware.hotelms.Models.Hotel.*;
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
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class HotelTest {
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
        List<HotelCreateDTO> hotelCreateDTOList = List.of(
                HotelCreateDTO.builder().name("Hotel 1").code("HOTEL1").description("Hotel 1 Description").build(),
                HotelCreateDTO.builder().name("Hotel 2").code("HOTEL2").description("Hotel 2 Description").build(),
                HotelCreateDTO.builder().name("Hotel 3").code("HOTEL3").description("Hotel 3 Description").build(),
                HotelCreateDTO.builder().name("Hotel 4").code("HOTEL4").description("Hotel 4 Description").build()
        );
        for (HotelCreateDTO dto : hotelCreateDTOList) {
            mvc.perform(MockMvcRequestBuilders.post("/hotels").
                    content(toJson(mapper, dto)).
                    characterEncoding(StandardCharsets.UTF_8).header("Authorization", "Bearer " + adminToken).
                    contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        }
    }

    @Test
    @Order(1)
    void listHotels() throws Exception {
        //Get list of Hotels as an Admin
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/hotels").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        List<HotelDTOSimpleAdmin> hotelDTOSimpleList = listFromJson(mapper, result, HotelDTOSimpleAdmin[].class);
        assertEquals(4, hotelDTOSimpleList.size());
        //Get list of Hotels as a regular User
        result = mvc.perform(MockMvcRequestBuilders.get("/hotels").
                        header("Authorization", "Bearer " + customerToken).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        List<HotelDTOSimple> hotelDTOSimpleList1 = listFromJson(mapper, result, HotelDTOSimple[].class);
        assertEquals(4, hotelDTOSimpleList1.size());
    }

    @Test
    @Order(2)
    void createHotel() throws Exception {
        //Get List of Hotels and Calculate Number of Hotels
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/hotels").
                header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).andReturn();
        List<HotelDTOSimpleAdmin> hotelDTOSimpleList = listFromJson(mapper, result, HotelDTOSimpleAdmin[].class);
        assertEquals(4, hotelDTOSimpleList.size());
        //Create HotelCreateDTO Object and POST it to create a new hotel
        String hotelName = "Hotel 5";
        String hotelDescription = "Hotel 5 Description";
        String hotelCode = "HOTEL5";
        HotelCreateDTO dto = HotelCreateDTO.builder().name(hotelName).description(hotelDescription).code(hotelCode).build();
        result = mvc.perform(MockMvcRequestBuilders.post("/hotels").
                        content(toJson(mapper, dto)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).
                andReturn();
        //Verify that the created Hotel object is correct according to the variables used to create it
        HotelDTOAdmin hotelDTOAdmin = fromJson(mapper, result, HotelDTOAdmin.class);
        assertEquals(hotelCode, hotelDTOAdmin.getCode());
        assertEquals(hotelName, hotelDTOAdmin.getName());
        assertEquals(hotelDescription, hotelDTOAdmin.getDescription());
        assertTrue(hotelDTOAdmin.isActive());
        assertEquals(0, hotelDTOAdmin.getRooms().size());
        assertEquals(0, hotelDTOAdmin.getServices().size());
        //Verify that it was added to the hotel list
        result = mvc.perform(MockMvcRequestBuilders.get("/hotels").
                header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).andReturn();
        hotelDTOSimpleList = listFromJson(mapper, result, HotelDTOSimpleAdmin[].class);
        assertEquals(5, hotelDTOSimpleList.size());
        assertTrue(hotelDTOSimpleList.stream().anyMatch(hotelDTOSimpleAdmin -> hotelDTOSimpleAdmin.getCode().equals(hotelCode)));
        //Expect authorization failure when creating a Hotel as a Customer
        mvc.perform(MockMvcRequestBuilders.post("/hotels").
                        content(toJson(mapper, dto)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + customerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isForbidden());
    }

    @Test
    @Order(3)
    void updateHotel() throws Exception {
        //Test the Update of Hotel 2, Updating its details and deactivating it
        String newHotelName = "Hotel 2 Name";
        String newHotelDescription = "New Hotel Description";
        String newHotelCode = "HOTEL2NEW";
        HotelUpdateDTO dto = HotelUpdateDTO.builder().name(newHotelName).description(newHotelDescription).code(newHotelCode).active(true).build();
        MvcResult result = mvc.perform(MockMvcRequestBuilders.put("/hotels/2").
                        content(toJson(mapper, dto)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).
                andReturn();
        HotelDTOAdmin hotelDTOAdmin = fromJson(mapper, result, HotelDTOAdmin.class);
        assertEquals(newHotelName, hotelDTOAdmin.getName());
        assertEquals(newHotelCode, hotelDTOAdmin.getCode());
        assertEquals(newHotelDescription, hotelDTOAdmin.getDescription());
        assertTrue(hotelDTOAdmin.isActive());
        //Expect authorization failure when updating a Hotel as a Customer
        mvc.perform(MockMvcRequestBuilders.put("/hotels/2").
                        content(toJson(mapper, dto)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + customerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isForbidden());
        //Ensure that the Hotel object from the getHotel endpoint matches the updated one
        result = mvc.perform(MockMvcRequestBuilders.get("/hotels/2").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        hotelDTOAdmin = fromJson(mapper, result, HotelDTOAdmin.class);
        assertEquals(newHotelName, hotelDTOAdmin.getName());
        assertEquals(newHotelCode, hotelDTOAdmin.getCode());
        assertEquals(newHotelDescription, hotelDTOAdmin.getDescription());
        //Set the Hotel as disabled (hidden for customers) and get Hotel list for administrator and customer
        dto = HotelUpdateDTO.builder().name(newHotelName).description(newHotelDescription).code(newHotelCode).active(false).build();
        result = mvc.perform(MockMvcRequestBuilders.put("/hotels/2").
                        content(toJson(mapper, dto)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).
                andReturn();
        hotelDTOAdmin = fromJson(mapper, result, HotelDTOAdmin.class);
        assertFalse(hotelDTOAdmin.isActive());
        result = mvc.perform(MockMvcRequestBuilders.get("/hotels").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        List<HotelDTOSimpleAdmin> hotelDTOSimpleList = listFromJson(mapper, result, HotelDTOSimpleAdmin[].class);
        assertEquals(5, hotelDTOSimpleList.size());
        //Assert that Hotel isn't accessible in Hotel list and is reported as not found for Customers
        result = mvc.perform(MockMvcRequestBuilders.get("/hotels").
                        header("Authorization", "Bearer " + customerToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        List<HotelDTOSimple> hotelDTOSimpleList1 = listFromJson(mapper, result, HotelDTOSimple[].class);
        assertEquals(4, hotelDTOSimpleList1.size());
        mvc.perform(MockMvcRequestBuilders.get("/hotels/2").
                        header("Authorization", "Bearer " + customerToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isNotFound());
    }

    @Test
    @Order(4)
    void getHotel() throws Exception {
        //Try to get a "Disabled" Hotel as a Customer and as an Admin
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/hotels/2").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        HotelDTOAdmin hotelDTOAdmin = fromJson(mapper, result, HotelDTOAdmin.class);
        assertFalse(hotelDTOAdmin.isActive());
        assertDoesNotThrow(() -> mvc.perform(MockMvcRequestBuilders.get("/hotels/2").
                        header("Authorization", "Bearer " + customerToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isNotFound()));
        //Get Hotel 1 as a Customer and an Administrator and ensure that it's available for both and its attributes match
        result = mvc.perform(MockMvcRequestBuilders.get("/hotels/1").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        hotelDTOAdmin = fromJson(mapper, result, HotelDTOAdmin.class);
        result = mvc.perform(MockMvcRequestBuilders.get("/hotels/1").
                        header("Authorization", "Bearer " + customerToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        HotelDTO hotelDTOUser = fromJson(mapper, result, HotelDTO.class);
        assertEquals(1, hotelDTOAdmin.getId());
        assertTrue(hotelDTOAdmin.isActive());
        assertEquals("Hotel 1", hotelDTOAdmin.getName());
        assertEquals(hotelDTOUser.getName(), hotelDTOAdmin.getName());
        assertEquals(hotelDTOUser.getCode(), hotelDTOAdmin.getCode());
        assertEquals(hotelDTOUser.getDescription(), hotelDTOAdmin.getDescription());
    }

    @Test
    @Order(5)
    void deleteHotel() throws Exception {
        //Ensure that Hotel 3 is Available for Customer and Admin
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/hotels/3").
                        header("Authorization", "Bearer " + customerToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        HotelDTO dto = fromJson(mapper, result, HotelDTO.class);
        assertEquals("HOTEL3", dto.getCode());
        mvc.perform(MockMvcRequestBuilders.get("/hotels/3").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());
        //Ensure that delete endpoint isn't accessible for Customers
        mvc.perform(MockMvcRequestBuilders.delete("/hotels/3").
                        header("Authorization", "Bearer " + customerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isForbidden());
        //Delete the Hotel and then assure it's gone for Admin and Customer
        assertDoesNotThrow(() -> mvc.perform(MockMvcRequestBuilders.delete("/hotels/3").
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()));
        assertDoesNotThrow(() -> mvc.perform(MockMvcRequestBuilders.get("/hotels/3").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isNotFound()));
        assertDoesNotThrow(() -> mvc.perform(MockMvcRequestBuilders.get("/hotels/3").
                        header("Authorization", "Bearer " + customerToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isNotFound()));
    }
}
