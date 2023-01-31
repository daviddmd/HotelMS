package com.trivialware.hotelms.Services;

import com.trivialware.hotelms.Configuration.JWTService;
import com.trivialware.hotelms.Entities.User;
import com.trivialware.hotelms.Enums.UserRole;
import com.trivialware.hotelms.Exceptions.CustomException;
import com.trivialware.hotelms.Models.User.UserLoginDTO;
import com.trivialware.hotelms.Models.User.UserRegisterDTO;
import com.trivialware.hotelms.Models.User.UserUpdateAdminDTO;
import com.trivialware.hotelms.Models.User.UserUpdateDTO;
import com.trivialware.hotelms.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public String login(UserLoginDTO userLoginDTO) {
        User user = userRepository.findByUsername(userLoginDTO.getUsernameOrEmail()).
                orElseGet(() -> userRepository.findByEmail(userLoginDTO.getUsernameOrEmail()).orElseThrow(() ->
                        new CustomException("No such user with this username/email", HttpStatus.BAD_REQUEST)));
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), userLoginDTO.getPassword()));
            return jwtTokenProvider.createToken(user.getUsername(), user.getRole(), userLoginDTO.isRememberMe());
        }
        catch (AuthenticationException e) {
            throw new CustomException("Invalid username/password supplied", HttpStatus.UNAUTHORIZED);
        }
    }

    public User register(UserRegisterDTO userRegisterDTO) {
        User userNameExists = userRepository.findByUsername(userRegisterDTO.getUsername()).orElse(null);
        User emailExists = userRepository.findByEmail(userRegisterDTO.getEmail()).orElse(null);
        if (userNameExists != null) {
            throw new CustomException("Username is already in use", HttpStatus.BAD_REQUEST);
        }
        if (emailExists != null) {
            throw new CustomException("Email is already in use", HttpStatus.BAD_REQUEST);
        }
        User user = User.builder().
                email(userRegisterDTO.getEmail()).
                first_name(userRegisterDTO.getFirst_name()).
                last_name(userRegisterDTO.getLast_name()).
                username(userRegisterDTO.getUsername()).
                phoneNumber(userRegisterDTO.getPhoneNumber()).
                enabled(true).
                role(userRepository.count() == 0 ? UserRole.ADMINISTRATOR : UserRole.CUSTOMER).
                password(passwordEncoder.encode(userRegisterDTO.getPassword())).
                build();
        user.setBookings(List.of());
        return userRepository.save(user);
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new CustomException(String.format("No User with ID %d exists.", id), HttpStatus.NOT_FOUND));
    }

    public User updateUserAdmin(User user, UserUpdateAdminDTO userUpdateAdminDTO) {
        updateUserDetails(user, userUpdateAdminDTO);
        user.setEnabled(userUpdateAdminDTO.isEnabled());
        user.setRole(userUpdateAdminDTO.getRole());
        if (userUpdateAdminDTO.getNewPassword() != null && !userUpdateAdminDTO.getNewPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userUpdateAdminDTO.getNewPassword()));
        }
        return userRepository.save(user);
    }

    private void updateUserDetails(User user, UserUpdateDTO userUpdateDTO) {
        user.setFirst_name(userUpdateDTO.getFirst_name());
        user.setLast_name(userUpdateDTO.getLast_name());
        if (userRepository.findByEmail(userUpdateDTO.getEmail()).isPresent() && !user.getEmail().equals(userUpdateDTO.getEmail())) {
            throw new CustomException("New Email is already in use.", HttpStatus.BAD_REQUEST);
        }
        user.setEmail(userUpdateDTO.getEmail());
        user.setPhoneNumber(userUpdateDTO.getPhoneNumber());
    }

    public User updateSelf(User user, UserUpdateDTO userUpdateDTO) {
        updateUserDetails(user, userUpdateDTO);
        if (
                userUpdateDTO.getNewPassword() != null &&
                        !userUpdateDTO.getNewPassword().isEmpty() &&
                        userUpdateDTO.getCurrentPassword() != null &&
                        !userUpdateDTO.getCurrentPassword().isEmpty()
        ) {
            if (passwordEncoder.matches(userUpdateDTO.getCurrentPassword(), user.getPassword())) {
                user.setPassword(passwordEncoder.encode(userUpdateDTO.getNewPassword()));
            }
            else {
                throw new CustomException("The current password is incorrect.", HttpStatus.BAD_REQUEST);
            }
        }
        return userRepository.save(user);
    }

    public void delete(User user) {
        user.setBookings(List.of());
        userRepository.delete(user);
    }

    public void deactivateUser(User user) {
        user.setEnabled(false);
        userRepository.save(user);
    }
}