package com.trivialware.hotelms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trivialware.hotelms.Models.Hotel.HotelCreateDTO;
import com.trivialware.hotelms.Models.Hotel.HotelDTOAdmin;
import com.trivialware.hotelms.Models.Hotel.HotelDTOSimpleAdmin;
import com.trivialware.hotelms.Models.ServiceHotel.ServiceHotelCreateDTO;
import com.trivialware.hotelms.Models.ServiceHotel.ServiceHotelDTO;
import com.trivialware.hotelms.Models.ServiceHotel.ServiceHotelUpdateDTO;
import com.trivialware.hotelms.Models.User.UserLoginDTO;
import com.trivialware.hotelms.Models.User.UserRegisterDTO;
import com.trivialware.hotelms.Services.HotelService;
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
public class HotelServiceTest {
    ObjectMapper mapper;
    String adminToken, customerToken;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserService userService;
    @Autowired
    private HotelService hotelService;

    @BeforeAll
    void setup() throws Exception {
        //Register Users and Get Administrator and Customer Tokens
        userService.register(UserRegisterDTO.builder().first_name(firstNameAdmin).last_name(lastNameAdmin).username(usernameAdmin).email(emailAdmin).password(passwordAdmin).phoneNumber(phoneNumberAdmin).build());
        userService.register(UserRegisterDTO.builder().first_name(firstNameFirstCustomer).last_name(lastNameFirstCustomer).username(usernameFirstCustomer).email(emailFirstCustomer).password(passwordFirstCustomer).phoneNumber(phoneNumberFirstCustomer).build());
        adminToken = userService.login(UserLoginDTO.builder().usernameOrEmail(emailAdmin).password(passwordAdmin).rememberMe(true).build());
        customerToken = userService.login(UserLoginDTO.builder().usernameOrEmail(emailFirstCustomer).password(passwordFirstCustomer).rememberMe(true).build());
        mapper = new ObjectMapper();
        //Register Hotels
        hotelService.addHotel(HotelCreateDTO.builder().name("Hotel 2").code("HOTEL2").description("Hotel 2 Description").build());
        hotelService.addHotel(HotelCreateDTO.builder().name("Hotel 3").code("HOTEL3").description("Hotel 3 Description").build());
        hotelService.addHotel(HotelCreateDTO.builder().name("Hotel 4").code("HOTEL4").description("Hotel 4 Description").build());
        //Create Services
        List<ServiceHotelCreateDTO> serviceHotelCreateDTOList = List.of(
                ServiceHotelCreateDTO.builder().name("Breakfast").description("Mediterranean Breakfast from 08:00 to 11:00").code("BREAKFAST1").cost(new BigDecimal(15)).hotelId(1L).build(),
                ServiceHotelCreateDTO.builder().name("Parking").description("Underground Parking Service").code("PARKING1").cost(new BigDecimal(15)).hotelId(1L).build(),
                ServiceHotelCreateDTO.builder().name("Breakfast").description("Mediterranean Breakfast from 08:00 to 11:00").code("BREAKFAST1").cost(new BigDecimal(15)).hotelId(2L).build(),
                ServiceHotelCreateDTO.builder().name("Parking").description("Underground Parking Service").code("PARKING1").cost(new BigDecimal(15)).hotelId(2L).build()
        );
        for (ServiceHotelCreateDTO dto : serviceHotelCreateDTOList) {
            mvc.perform(MockMvcRequestBuilders.post("/services").
                    content(toJson(mapper, dto)).
                    characterEncoding(StandardCharsets.UTF_8).header("Authorization", "Bearer " + adminToken).
                    contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        }
    }

    @Test
    @Order(1)
    void listServices() throws Exception {
        //List all services and check for service existence in Hotel (get by ID and number of services)
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/services").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        List<ServiceHotelDTO> serviceHotelDTOList = listFromJson(mapper, result, ServiceHotelDTO[].class);
        assertEquals(4, serviceHotelDTOList.size());
        assertTrue(serviceHotelDTOList.stream().anyMatch(serviceHotelDTO -> serviceHotelDTO.getCode().equals("BREAKFAST1") && serviceHotelDTO.getHotelId() == 1));
        assertTrue(serviceHotelDTOList.stream().anyMatch(serviceHotelDTO -> serviceHotelDTO.getCode().equals("PARKING1") && serviceHotelDTO.getHotelId() == 1));
        assertTrue(serviceHotelDTOList.stream().anyMatch(serviceHotelDTO -> serviceHotelDTO.getCode().equals("BREAKFAST1") && serviceHotelDTO.getHotelId() == 2));
        assertTrue(serviceHotelDTOList.stream().anyMatch(serviceHotelDTO -> serviceHotelDTO.getCode().equals("PARKING1") && serviceHotelDTO.getHotelId() == 2));
        //Assert that are two services in each hotel in the hotel listing response (number of rooms+services for each hotel)
        result = mvc.perform(MockMvcRequestBuilders.get("/hotels").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        List<HotelDTOSimpleAdmin> hotelDTOSimpleList = listFromJson(mapper, result, HotelDTOSimpleAdmin[].class);
        assertEquals(2, hotelDTOSimpleList.stream().filter(hotelDTOSimpleAdmin -> hotelDTOSimpleAdmin.getId() == 1).findFirst().orElseThrow().getNumberServices());
        assertEquals(2, hotelDTOSimpleList.stream().filter(hotelDTOSimpleAdmin -> hotelDTOSimpleAdmin.getId() == 2).findFirst().orElseThrow().getNumberServices());
        result = mvc.perform(MockMvcRequestBuilders.get("/hotels/1").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        HotelDTOAdmin hotelDTOAdmin = fromJson(mapper, result, HotelDTOAdmin.class);
        //Assert that both services are present in the services list for Hotel 1
        assertTrue(hotelDTOAdmin.getServices().stream().anyMatch(serviceHotelDTO -> serviceHotelDTO.getCode().equals("BREAKFAST1")));
        assertTrue(hotelDTOAdmin.getServices().stream().anyMatch(serviceHotelDTO -> serviceHotelDTO.getCode().equals("PARKING1")));
        //Assert that Customers can't list services
        mvc.perform(MockMvcRequestBuilders.get("/services").
                        header("Authorization", "Bearer " + customerToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isForbidden());
    }

    @Test
    @Order(2)
    void createService() throws Exception {
        String newServiceName = "Spa Service";
        String newServiceDescription = "In-Hotel Spa Service";
        String newServiceCode = "SPA1";
        BigDecimal newServiceCost = BigDecimal.valueOf(15.55);
        long newServiceHotelId = 1;
        ServiceHotelCreateDTO serviceHotelCreateDTO = ServiceHotelCreateDTO.builder().
                name(newServiceName).
                code(newServiceCode).
                description(newServiceDescription).
                hotelId(newServiceHotelId).
                cost(newServiceCost).
                build();
        MvcResult result = mvc.perform(MockMvcRequestBuilders.post("/services").
                        content(toJson(mapper, serviceHotelCreateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).
                andReturn();
        //Verify that the return object is correct
        ServiceHotelDTO serviceHotelDTO = fromJson(mapper, result, ServiceHotelDTO.class);
        assertEquals(newServiceName, serviceHotelDTO.getName());
        assertEquals(newServiceHotelId, serviceHotelDTO.getHotelId());
        assertEquals(newServiceDescription, serviceHotelDTO.getDescription());
        assertEquals(newServiceCode, serviceHotelDTO.getCode());
        assertEquals(newServiceCost, serviceHotelDTO.getCost());
        result = mvc.perform(MockMvcRequestBuilders.get("/services/5").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        serviceHotelDTO = fromJson(mapper, result, ServiceHotelDTO.class);
        assertEquals(newServiceName, serviceHotelDTO.getName());
        assertEquals(newServiceHotelId, serviceHotelDTO.getHotelId());
        assertEquals(newServiceDescription, serviceHotelDTO.getDescription());
        assertEquals(newServiceCode, serviceHotelDTO.getCode());
        assertEquals(newServiceCost, serviceHotelDTO.getCost());
        //Assert that service is present in the hotel
        result = mvc.perform(MockMvcRequestBuilders.get("/hotels/1").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        HotelDTOAdmin hotelDTOAdmin = fromJson(mapper, result, HotelDTOAdmin.class);
        assertEquals(3, hotelDTOAdmin.getServices().size());
        assertTrue(hotelDTOAdmin.getServices().stream().anyMatch(service -> service.getCode().equals(newServiceCode)));
        //Assert that creating a new service with the same code as an existent service in the same Hotel fails
        serviceHotelCreateDTO = ServiceHotelCreateDTO.builder().
                name("New Service Name").
                code(newServiceCode).
                description("New Service Description").
                hotelId(newServiceHotelId).
                cost(newServiceCost).
                build();
        mvc.perform(MockMvcRequestBuilders.post("/services").
                        content(toJson(mapper, serviceHotelCreateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isBadRequest());
        //Expect the creation to succeed when creating on a Hotel (2) that doesn't have a service with an existent ID (SPA1)
        serviceHotelCreateDTO = ServiceHotelCreateDTO.builder().
                name(newServiceName).
                code(newServiceCode).
                description(newServiceDescription).
                hotelId(2L).
                cost(newServiceCost).
                build();
        result = mvc.perform(MockMvcRequestBuilders.post("/services").
                        content(toJson(mapper, serviceHotelCreateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).
                andReturn();
        serviceHotelDTO = fromJson(mapper, result, ServiceHotelDTO.class);
        assertEquals(2, serviceHotelDTO.getHotelId());
        assertEquals(6, serviceHotelDTO.getId());
        //Assert that Customers can't create services
        mvc.perform(MockMvcRequestBuilders.post("/services").
                        content(toJson(mapper, serviceHotelCreateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + customerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isForbidden());
    }

    @Test
    @Order(3)
    void updateService() throws Exception {
        String newServiceName = "New Parking Service";
        String newServiceDescription = "Underground Parking Service";
        String newServiceCode = "NEWPARKING1";
        BigDecimal newServiceCost = BigDecimal.valueOf(99.21);
        ServiceHotelUpdateDTO serviceHotelUpdateDTO = ServiceHotelUpdateDTO.builder().
                name(newServiceName).
                description(newServiceDescription).
                code(newServiceCode).
                cost(newServiceCost).
                build();
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/services/2").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        ServiceHotelDTO serviceHotelDTO = fromJson(mapper, result, ServiceHotelDTO.class);
        assertEquals("PARKING1", serviceHotelDTO.getCode());
        assertEquals(1, serviceHotelDTO.getHotelId());
        result = mvc.perform(MockMvcRequestBuilders.put("/services/2").
                        content(toJson(mapper, serviceHotelUpdateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).
                andReturn();
        serviceHotelDTO = fromJson(mapper, result, ServiceHotelDTO.class);
        assertEquals(newServiceName, serviceHotelDTO.getName());
        assertEquals(newServiceDescription, serviceHotelDTO.getDescription());
        assertEquals(newServiceCode, serviceHotelDTO.getCode());
        assertEquals(newServiceCost, serviceHotelDTO.getCost());
        assertEquals(1, serviceHotelDTO.getHotelId());
        result = mvc.perform(MockMvcRequestBuilders.get("/services/2").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        serviceHotelDTO = fromJson(mapper, result, ServiceHotelDTO.class);
        assertEquals(newServiceName, serviceHotelDTO.getName());
        assertEquals(newServiceDescription, serviceHotelDTO.getDescription());
        assertEquals(newServiceCode, serviceHotelDTO.getCode());
        assertEquals(newServiceCost, serviceHotelDTO.getCost());
        assertEquals(1, serviceHotelDTO.getHotelId());
        result = mvc.perform(MockMvcRequestBuilders.get("/hotels/1").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        HotelDTOAdmin hotelDTOAdmin = fromJson(mapper, result, HotelDTOAdmin.class);
        assertTrue(hotelDTOAdmin.getServices().stream().anyMatch(service -> service.getCode().equals(newServiceCode)));
        //Assert that a service code can't be updated to an existent one
        serviceHotelUpdateDTO = ServiceHotelUpdateDTO.builder().
                name(newServiceName).
                description(newServiceDescription).
                code("BREAKFAST1").
                cost(newServiceCost).
                build();
        mvc.perform(MockMvcRequestBuilders.put("/services/2").
                        content(toJson(mapper, serviceHotelUpdateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isBadRequest());
        //Assert that a service can be updated with the same code
        serviceHotelUpdateDTO = ServiceHotelUpdateDTO.builder().
                name("Random Name").
                description("Random Description").
                code(newServiceCode).
                cost(newServiceCost).
                build();
        mvc.perform(MockMvcRequestBuilders.put("/services/2").
                        content(toJson(mapper, serviceHotelUpdateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());
        //Assert that customers can't update services
        mvc.perform(MockMvcRequestBuilders.put("/services/2").
                        content(toJson(mapper, serviceHotelUpdateDTO)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + customerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isForbidden());
    }

    @Test
    @Order(5)
    void deleteService() throws Exception {
        //Assert that the service exists
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/services/2").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        ServiceHotelDTO serviceHotelDTO = fromJson(mapper, result, ServiceHotelDTO.class);
        assertEquals("NEWPARKING1", serviceHotelDTO.getCode());
        assertEquals(1, serviceHotelDTO.getHotelId());
        //Get the current number of services in Hotel 1 (the hotel associated with Service 2)
        result = mvc.perform(MockMvcRequestBuilders.get("/hotels").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        List<HotelDTOSimpleAdmin> hotelDTOSimpleList = listFromJson(mapper, result, HotelDTOSimpleAdmin[].class);
        int numberServices = hotelDTOSimpleList.stream().filter(h -> h.getId() == 1).findFirst().orElseThrow().getNumberServices();
        //Assert that the service is present in Hotel 1
        result = mvc.perform(MockMvcRequestBuilders.get("/hotels/1").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        HotelDTOAdmin hotelDTOAdmin = fromJson(mapper, result, HotelDTOAdmin.class);
        assertTrue(hotelDTOAdmin.getServices().stream().anyMatch(service -> service.getCode().equals(serviceHotelDTO.getCode())));
        //Delete the Service
        mvc.perform(MockMvcRequestBuilders.delete("/services/2").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());
        //Assert that it is deleted
        mvc.perform(MockMvcRequestBuilders.get("/services/2").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isNotFound());
        //Assert that the service isn't present in Hotel 1
        result = mvc.perform(MockMvcRequestBuilders.get("/hotels/1").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        hotelDTOAdmin = fromJson(mapper, result, HotelDTOAdmin.class);
        assertFalse(hotelDTOAdmin.getServices().stream().anyMatch(service -> service.getCode().equals(serviceHotelDTO.getCode())));
        //Assert that the number of services associated with Hotel 1 has decreased by 1
        result = mvc.perform(MockMvcRequestBuilders.get("/hotels").
                        header("Authorization", "Bearer " + adminToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        hotelDTOSimpleList = listFromJson(mapper, result, HotelDTOSimpleAdmin[].class);
        assertEquals(numberServices - 1, hotelDTOSimpleList.stream().filter(h -> h.getId() == 1).findFirst().orElseThrow().getNumberServices());
        //Assert that Customers can't delete services
        mvc.perform(MockMvcRequestBuilders.delete("/services/2").
                        header("Authorization", "Bearer " + customerToken).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isForbidden());
    }
}
