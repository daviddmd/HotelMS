package com.trivialware.hotelms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.trivialware.hotelms.Configuration.JWTService;
import com.trivialware.hotelms.Entities.User;
import com.trivialware.hotelms.Enums.UserRole;
import com.trivialware.hotelms.Models.User.*;
import com.trivialware.hotelms.Repositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
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

import java.io.UnsupportedEncodingException;
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
public class UserTest {
    String adminToken;
    String firstCustomerToken;
    ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JWTService jwtService;

    @BeforeAll
    public void setup() throws Exception {
        mapper = new ObjectMapper();
        UserRegisterDTO adminRegisterDTO, employeeRegisterDTO, firstCustomerRegisterDTO, secondCustomerRegisterDTO;
        adminRegisterDTO = UserRegisterDTO.builder().first_name(firstNameAdmin).last_name(lastNameAdmin).username(usernameAdmin).email(emailAdmin).password(passwordAdmin).phoneNumber(phoneNumberAdmin).build();
        employeeRegisterDTO = UserRegisterDTO.builder().first_name(firstNameEmployee).last_name(lastNameEmployee).username(usernameEmployee).email(emailEmployee).password(passwordEmployee).phoneNumber(phoneNumberEmployee).build();
        firstCustomerRegisterDTO = UserRegisterDTO.builder().first_name(firstNameFirstCustomer).last_name(lastNameFirstCustomer).username(usernameFirstCustomer).email(emailFirstCustomer).password(passwordFirstCustomer).phoneNumber(phoneNumberFirstCustomer).build();
        secondCustomerRegisterDTO = UserRegisterDTO.builder().first_name(firstNameSecondCustomer).last_name(lastNameSecondCustomer).username(usernameSecondCustomer).email(emailSecondCustomer).password(passwordSecondCustomer).phoneNumber(phoneNumberSecondCustomer).build();
        mvc.perform(MockMvcRequestBuilders.post("/auth/register").
                        content(toJson(mapper, adminRegisterDTO)).
                        characterEncoding(StandardCharsets.UTF_8).
                        contentType(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());
        mvc.perform(MockMvcRequestBuilders.post("/auth/register").
                        content(toJson(mapper, employeeRegisterDTO)).
                        characterEncoding(StandardCharsets.UTF_8).
                        contentType(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());
        mvc.perform(MockMvcRequestBuilders.post("/auth/register").
                        content(toJson(mapper, firstCustomerRegisterDTO)).
                        characterEncoding(StandardCharsets.UTF_8).
                        contentType(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());
        mvc.perform(MockMvcRequestBuilders.post("/auth/register").
                        content(toJson(mapper, secondCustomerRegisterDTO)).
                        characterEncoding(StandardCharsets.UTF_8).
                        contentType(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());
        mvc.perform(MockMvcRequestBuilders.post("/auth/register").
                        content(toJson(mapper, secondCustomerRegisterDTO)).
                        characterEncoding(StandardCharsets.UTF_8).
                        contentType(MediaType.APPLICATION_JSON)).
                andExpect(status().isBadRequest());
        UserLoginDTO dto = UserLoginDTO.builder().usernameOrEmail(emailAdmin).password(passwordAdmin).rememberMe(true).build();
        MvcResult result = mvc.perform(MockMvcRequestBuilders.post("/auth/login").
                content(toJson(mapper, dto)).
                characterEncoding(StandardCharsets.UTF_8).
                contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
        adminToken = getTokenFromResponse(result);
        dto = UserLoginDTO.builder().usernameOrEmail(emailFirstCustomer).password(passwordFirstCustomer).rememberMe(true).build();
        result = mvc.perform(MockMvcRequestBuilders.post("/auth/login").
                content(toJson(mapper, dto)).
                characterEncoding(StandardCharsets.UTF_8).
                contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
        firstCustomerToken = getTokenFromResponse(result);

    }

    @Test
    @Order(1)
    public void verifyDefaultInformation() {
        User admin = userRepository.findByUsername(usernameAdmin).orElse(null);
        assertNotNull(admin);
        assertEquals(admin.getRole(), UserRole.ADMINISTRATOR);
        assertEquals(emailAdmin, admin.getEmail());
        User employee = userRepository.findByUsername(usernameEmployee).orElse(null);
        assertNotNull(employee);
        assertEquals(employee.getRole(), UserRole.CUSTOMER);
        assertEquals(emailEmployee, employee.getEmail());
    }

    @Test
    @Order(2)
    public void login() throws Exception {
        UserLoginDTO dto = UserLoginDTO.builder().usernameOrEmail(emailAdmin).password(passwordAdmin).rememberMe(true).build();
        MvcResult result = mvc.perform(MockMvcRequestBuilders.post("/auth/login").
                content(toJson(mapper, dto)).
                characterEncoding(StandardCharsets.UTF_8).
                contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
        //Test validity of JSON tokens, checking if the returned token subject is the same username as the administrator's
        String token = getTokenFromResponse(result);
        assertFalse(token.isEmpty());
        Claims claims = Jwts.parser().setSigningKey(jwtService.getSecretKey()).parseClaimsJws(token).getBody();
        assertEquals(usernameAdmin, claims.getSubject());
        dto = UserLoginDTO.builder().usernameOrEmail(emailAdmin).password(passwordSecondCustomer).rememberMe(true).build();
        mvc.perform(MockMvcRequestBuilders.post("/auth/login").
                        content(toJson(mapper, dto)).
                        characterEncoding(StandardCharsets.UTF_8).
                        contentType(MediaType.APPLICATION_JSON)).
                andExpect(status().isUnauthorized());
    }

    @Test
    @Order(3)
    public void register() throws Exception {
        //Create new User
        String newUserFirstName = "John";
        String newUserLastName = "Doe";
        String newUserPhoneNumber = "950000000";
        String newUserUsername = "johndoe";
        String newUserEmail = "john.doe@example.com";
        String newUserPassword = "password";
        UserRegisterDTO dto = UserRegisterDTO.builder().
                first_name(newUserFirstName).
                last_name(newUserLastName).
                phoneNumber(newUserPhoneNumber).
                username(newUserUsername).
                email(newUserEmail).
                password(newUserPassword).
                build();
        MvcResult result = mvc.perform(MockMvcRequestBuilders.post("/auth/register").
                        content(toJson(mapper, dto)).
                        characterEncoding(StandardCharsets.UTF_8).
                        contentType(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        UserDTO userDTO = fromJson(mapper, result, UserDTO.class);
        assertEquals(newUserFirstName, userDTO.getFirst_name());
        assertEquals(newUserLastName, userDTO.getLast_name());
        assertEquals(newUserUsername, userDTO.getUsername());
        assertEquals(newUserEmail, userDTO.getEmail());
        assertEquals(newUserPhoneNumber, userDTO.getPhoneNumber());
        //Assert User exists
        result = mvc.perform(
                        MockMvcRequestBuilders.get("/users").
                                header("Authorization", "Bearer " + adminToken).
                                accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        List<UserDTOAdmin> usersList = listFromJson(mapper, result, UserDTOAdmin[].class);
        assertEquals(5, usersList.size());
        assertTrue(usersList.stream().anyMatch(
                        userDTOAdmin ->
                                userDTOAdmin.getUsername().equals(newUserUsername) &&
                                        userDTOAdmin.getEmail().equals(newUserEmail) &&
                                        userDTOAdmin.getFirst_name().equals(newUserFirstName) &&
                                        userDTOAdmin.getLast_name().equals(newUserLastName) &&
                                        userDTOAdmin.getPhoneNumber().equals(newUserPhoneNumber)
                )
        );
        //Try to create User with existing username
        dto = UserRegisterDTO.builder().
                first_name(newUserFirstName).
                last_name(newUserLastName).
                phoneNumber(newUserPhoneNumber).
                username(newUserUsername).
                email("random@random.com").
                password(newUserPassword).
                build();
        mvc.perform(MockMvcRequestBuilders.post("/auth/register").
                        content(toJson(mapper, dto)).
                        characterEncoding(StandardCharsets.UTF_8).
                        contentType(MediaType.APPLICATION_JSON)).
                andExpect(status().isBadRequest());
        //Try to create User with existing email
        dto = UserRegisterDTO.builder().
                first_name(newUserFirstName).
                last_name(newUserLastName).
                phoneNumber(newUserPhoneNumber).
                username("random").
                email(newUserEmail).
                password(newUserPassword).
                build();
        mvc.perform(MockMvcRequestBuilders.post("/auth/register").
                        content(toJson(mapper, dto)).
                        characterEncoding(StandardCharsets.UTF_8).
                        contentType(MediaType.APPLICATION_JSON)).
                andExpect(status().isBadRequest());

    }

    @Test
    @Order(4)
    void listUsers() throws Exception {
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/users").
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        List<UserDTOAdmin> usersList = listFromJson(mapper, result, UserDTOAdmin[].class);
        assertEquals(5, usersList.size());
        //Ensure that Customers can't list Users
        mvc.perform(MockMvcRequestBuilders.get("/users").
                        header("Authorization", "Bearer " + firstCustomerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isForbidden());
    }

    @Test
    @Order(5)
    void getUser() throws Exception {
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/users/3").
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        UserDTOAdmin userDTOAdmin = fromJson(mapper, result, UserDTOAdmin.class);
        assertEquals(firstNameFirstCustomer, userDTOAdmin.getFirst_name());
        assertEquals(lastNameFirstCustomer, userDTOAdmin.getLast_name());
        assertEquals(emailFirstCustomer, userDTOAdmin.getEmail());
        assertEquals(usernameFirstCustomer, userDTOAdmin.getUsername());
        assertEquals(phoneNumberFirstCustomer, userDTOAdmin.getPhoneNumber());
        //Ensure that Customers can't get Users by their identifier
        assertDoesNotThrow(
                () ->
                        mvc.perform(MockMvcRequestBuilders.get("/users/3").
                                        header("Authorization", "Bearer " + firstCustomerToken).
                                        accept(MediaType.APPLICATION_JSON)).
                                andExpect(status().isForbidden())
        );
    }

    @Test
    @Order(6)
    void updateUser() throws Exception {
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/users/3").
                        header("Authorization", "Bearer " + adminToken).
                        characterEncoding(StandardCharsets.UTF_8).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        UserDTOAdmin userDTOAdmin = fromJson(mapper, result, UserDTOAdmin.class);
        //Verify Current User Details (First Customer) are Correct
        assertEquals(firstNameFirstCustomer, userDTOAdmin.getFirst_name());
        assertEquals(lastNameFirstCustomer, userDTOAdmin.getLast_name());
        assertEquals(emailFirstCustomer, userDTOAdmin.getEmail());
        assertEquals(usernameFirstCustomer, userDTOAdmin.getUsername());
        assertEquals(phoneNumberFirstCustomer, userDTOAdmin.getPhoneNumber());
        String newFirstName = String.format("%s %s", "New", firstNameFirstCustomer);
        String newLastName = String.format("%s %s", "New", lastNameFirstCustomer);
        String newEmail = String.format("%s.%s", "new", emailFirstCustomer);
        String newPhoneNumber = String.format("%s-%s", "NEW-", phoneNumberFirstCustomer);
        String newPassword = String.format("%s%s", "new", passwordFirstCustomer);
        //Login with Current Email before changing it
        UserLoginDTO loginDTO = UserLoginDTO.builder().usernameOrEmail(emailFirstCustomer).password(passwordFirstCustomer).rememberMe(true).build();
        mvc.perform(MockMvcRequestBuilders.post("/auth/login").
                content(toJson(mapper, loginDTO)).
                characterEncoding(StandardCharsets.UTF_8).
                contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        //Update First Customer's details
        UserUpdateAdminDTO dto = UserUpdateAdminDTO.builder().
                first_name(newFirstName).
                last_name(newLastName).
                email(newEmail).
                phoneNumber(newPhoneNumber).
                role(UserRole.CUSTOMER).
                enabled(true).
                build();
        result = mvc.perform(MockMvcRequestBuilders.put("/users/3").
                        content(toJson(mapper, dto)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).
                andReturn();
        //Check if update response details are correct
        userDTOAdmin = fromJson(mapper, result, UserDTOAdmin.class);
        assertEquals(newFirstName, userDTOAdmin.getFirst_name());
        assertEquals(newLastName, userDTOAdmin.getLast_name());
        assertEquals(newEmail, userDTOAdmin.getEmail());
        assertEquals(usernameFirstCustomer, userDTOAdmin.getUsername());
        assertEquals(newPhoneNumber, userDTOAdmin.getPhoneNumber());
        //Check if details from user endpoint are updated
        result = mvc.perform(MockMvcRequestBuilders.get("/users/3").
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        userDTOAdmin = fromJson(mapper, result, UserDTOAdmin.class);
        assertEquals(newFirstName, userDTOAdmin.getFirst_name());
        assertEquals(newLastName, userDTOAdmin.getLast_name());
        assertEquals(newEmail, userDTOAdmin.getEmail());
        assertEquals(usernameFirstCustomer, userDTOAdmin.getUsername());
        assertEquals(newPhoneNumber, userDTOAdmin.getPhoneNumber());
        //login first user with new email/username and current password, then update password and log in again to confirm it changed
        loginDTO = UserLoginDTO.builder().usernameOrEmail(usernameFirstCustomer).password(passwordFirstCustomer).rememberMe(true).build();
        mvc.perform(MockMvcRequestBuilders.post("/auth/login").
                content(toJson(mapper, loginDTO)).
                characterEncoding(StandardCharsets.UTF_8).
                contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        loginDTO = UserLoginDTO.builder().usernameOrEmail(newEmail).password(passwordFirstCustomer).rememberMe(true).build();
        mvc.perform(MockMvcRequestBuilders.post("/auth/login").
                content(toJson(mapper, loginDTO)).
                characterEncoding(StandardCharsets.UTF_8).
                contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        dto = UserUpdateAdminDTO.builder().
                first_name(newFirstName).
                last_name(newLastName).
                email(newEmail).
                phoneNumber(newPhoneNumber).
                newPassword(newPassword).
                role(UserRole.CUSTOMER).
                enabled(true).
                build();
        mvc.perform(MockMvcRequestBuilders.put("/users/3").
                        content(toJson(mapper, dto)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());
        loginDTO = UserLoginDTO.builder().usernameOrEmail(newEmail).password(newPassword).rememberMe(true).build();
        mvc.perform(MockMvcRequestBuilders.post("/auth/login").
                content(toJson(mapper, loginDTO)).
                characterEncoding(StandardCharsets.UTF_8).
                contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        //Reset to Default Values
        dto = UserUpdateAdminDTO.builder().
                first_name(firstNameFirstCustomer).
                last_name(lastNameFirstCustomer).
                email(emailFirstCustomer).
                phoneNumber(phoneNumberFirstCustomer).
                newPassword(passwordFirstCustomer).
                role(UserRole.CUSTOMER).
                enabled(true).
                build();
        mvc.perform(MockMvcRequestBuilders.put("/users/3").
                        content(toJson(mapper, dto)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());
        //Ensure that regular users can't update Users
        mvc.perform(MockMvcRequestBuilders.put("/users/3").
                        content(toJson(mapper, dto)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + firstCustomerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isForbidden());
        //Disable and try to login
        dto = UserUpdateAdminDTO.builder().
                first_name(firstNameFirstCustomer).
                last_name(lastNameFirstCustomer).
                email(emailFirstCustomer).
                phoneNumber(phoneNumberFirstCustomer).
                newPassword(passwordFirstCustomer).
                role(UserRole.CUSTOMER).
                enabled(false).
                build();
        mvc.perform(MockMvcRequestBuilders.put("/users/3").
                        content(toJson(mapper, dto)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());
        loginDTO = UserLoginDTO.builder().usernameOrEmail(emailFirstCustomer).password(passwordFirstCustomer).rememberMe(true).build();
        mvc.perform(MockMvcRequestBuilders.post("/auth/login").
                content(toJson(mapper, loginDTO)).
                characterEncoding(StandardCharsets.UTF_8).
                contentType(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        //Reactivate and set User to admin and try to access privileged functions
        dto = UserUpdateAdminDTO.builder().
                first_name(firstNameFirstCustomer).
                last_name(lastNameFirstCustomer).
                email(emailFirstCustomer).
                phoneNumber(phoneNumberFirstCustomer).
                newPassword(passwordFirstCustomer).
                role(UserRole.ADMINISTRATOR).
                enabled(true).
                build();
        mvc.perform(MockMvcRequestBuilders.put("/users/3").
                        content(toJson(mapper, dto)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());
        result = mvc.perform(MockMvcRequestBuilders.post("/auth/login").
                content(toJson(mapper, loginDTO)).
                characterEncoding(StandardCharsets.UTF_8).
                contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
        firstCustomerToken = getTokenFromResponse(result);
        mvc.perform(MockMvcRequestBuilders.get("/users/3").
                        header("Authorization", "Bearer " + firstCustomerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());
        //Reset User to Initial State
        dto = UserUpdateAdminDTO.builder().
                first_name(firstNameFirstCustomer).
                last_name(lastNameFirstCustomer).
                email(emailFirstCustomer).
                phoneNumber(phoneNumberFirstCustomer).
                newPassword(passwordFirstCustomer).
                role(UserRole.CUSTOMER).
                enabled(true).
                build();
        mvc.perform(MockMvcRequestBuilders.put("/users/3").
                        content(toJson(mapper, dto)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());
    }

    @Test
    @Order(7)
    void deleteUser() throws Exception {
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/users/4").
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        UserDTOAdmin userDTOAdmin = fromJson(mapper, result, UserDTOAdmin.class);
        //Verify Current User Details (Second Customer) are Correct
        assertEquals(firstNameSecondCustomer, userDTOAdmin.getFirst_name());
        assertEquals(lastNameSecondCustomer, userDTOAdmin.getLast_name());
        assertEquals(emailSecondCustomer, userDTOAdmin.getEmail());
        assertEquals(usernameSecondCustomer, userDTOAdmin.getUsername());
        assertEquals(phoneNumberSecondCustomer, userDTOAdmin.getPhoneNumber());
        mvc.perform(MockMvcRequestBuilders.delete("/users/4").
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());
        mvc.perform(MockMvcRequestBuilders.get("/users/4").
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isNotFound());
        //Ensure that regular users can't delete Users
        mvc.perform(MockMvcRequestBuilders.delete("/users/4").
                        header("Authorization", "Bearer " + firstCustomerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isForbidden());
    }

    @Test
    @Order(8)
    void getSelf() throws Exception {
        //Get First Customer Own Profile
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/users/self").
                        header("Authorization", "Bearer " + firstCustomerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        UserDTO userDTO = fromJson(mapper, result, UserDTO.class);
        assertEquals(firstNameFirstCustomer, userDTO.getFirst_name());
        assertEquals(lastNameFirstCustomer, userDTO.getLast_name());
        assertEquals(emailFirstCustomer, userDTO.getEmail());
        assertEquals(usernameFirstCustomer, userDTO.getUsername());
        assertEquals(phoneNumberFirstCustomer, userDTO.getPhoneNumber());
    }

    @Test
    @Order(9)
    void updateSelf() throws Exception {
        String newFirstName = String.format("%s %s", "New", firstNameFirstCustomer);
        String newLastName = String.format("%s %s", "New", lastNameFirstCustomer);
        String newEmail = String.format("%s.%s", "new", emailFirstCustomer);
        String newPhoneNumber = String.format("%s-%s", "NEW-", phoneNumberFirstCustomer);
        String newPassword = String.format("%s%s", "new", passwordFirstCustomer);
        UserUpdateDTO dto = UserUpdateDTO.builder().
                first_name(newFirstName).
                last_name(newLastName).
                email(newEmail).
                phoneNumber(newPhoneNumber).
                currentPassword(passwordFirstCustomer).
                newPassword(newPassword).build();
        MvcResult result = mvc.perform(MockMvcRequestBuilders.put("/users/self").
                        content(toJson(mapper, dto)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + firstCustomerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).
                andReturn();
        UserDTO userDTO = fromJson(mapper, result, UserDTO.class);
        assertEquals(newFirstName, userDTO.getFirst_name());
        assertEquals(newLastName, userDTO.getLast_name());
        assertEquals(newEmail, userDTO.getEmail());
        assertEquals(newPhoneNumber, userDTO.getPhoneNumber());
        result = mvc.perform(MockMvcRequestBuilders.get("/users/self").
                        header("Authorization", "Bearer " + firstCustomerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        userDTO = fromJson(mapper, result, UserDTO.class);
        assertEquals(newFirstName, userDTO.getFirst_name());
        assertEquals(newLastName, userDTO.getLast_name());
        assertEquals(newEmail, userDTO.getEmail());
        assertEquals(newPhoneNumber, userDTO.getPhoneNumber());
        //assert update password fails when current password is incorrect
        dto = UserUpdateDTO.builder().
                first_name(newFirstName).
                last_name(newLastName).
                email(newEmail).
                phoneNumber(newPhoneNumber).
                currentPassword("wrongpassword").
                newPassword("newpassword").build();
        mvc.perform(MockMvcRequestBuilders.put("/users/self").
                        content(toJson(mapper, dto)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + firstCustomerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isBadRequest());
        //reset user details
        dto = UserUpdateDTO.builder().
                first_name(firstNameFirstCustomer).
                last_name(lastNameFirstCustomer).
                email(emailFirstCustomer).
                phoneNumber(phoneNumberFirstCustomer).
                currentPassword(newPassword).
                newPassword(passwordFirstCustomer).build();
        mvc.perform(MockMvcRequestBuilders.put("/users/self").
                        content(toJson(mapper, dto)).
                        contentType(MediaType.APPLICATION_JSON).
                        characterEncoding(StandardCharsets.UTF_8).
                        header("Authorization", "Bearer " + firstCustomerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());
    }

    @Test
    @Order(10)
    void deactivateSelf() throws Exception {
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/users/3").
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        UserDTOAdmin userDTOAdmin = fromJson(mapper, result, UserDTOAdmin.class);
        assertTrue(userDTOAdmin.isEnabled());
        mvc.perform(MockMvcRequestBuilders.delete("/users/self").
                        header("Authorization", "Bearer " + firstCustomerToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());
        result = mvc.perform(MockMvcRequestBuilders.get("/users/3").
                        header("Authorization", "Bearer " + adminToken).
                        accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        userDTOAdmin = fromJson(mapper, result, UserDTOAdmin.class);
        assertFalse(userDTOAdmin.isEnabled());
        UserLoginDTO userLoginDTO = UserLoginDTO.builder().usernameOrEmail(emailFirstCustomer).password(passwordFirstCustomer).rememberMe(true).build();
        mvc.perform(MockMvcRequestBuilders.post("/auth/login").
                content(toJson(mapper, userLoginDTO)).
                characterEncoding(StandardCharsets.UTF_8).
                contentType(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
    }

    String getTokenFromResponse(MvcResult result) throws UnsupportedEncodingException {
        return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }
}
