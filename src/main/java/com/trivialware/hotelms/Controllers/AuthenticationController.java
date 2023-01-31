package com.trivialware.hotelms.Controllers;

import com.trivialware.hotelms.Models.User.AuthenticationResponse;
import com.trivialware.hotelms.Models.User.UserDTO;
import com.trivialware.hotelms.Models.User.UserLoginDTO;
import com.trivialware.hotelms.Models.User.UserRegisterDTO;
import com.trivialware.hotelms.Services.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Tag(name = "Authentication")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final UserService userService;
    private final ModelMapper mapper;

    @PostMapping("/register")
    public UserDTO signup(@RequestBody @Valid UserRegisterDTO userRegisterDTO) {
        return mapper.map(userService.register(userRegisterDTO), UserDTO.class);
    }

    @PostMapping("/login")
    public AuthenticationResponse login(@RequestBody @Valid UserLoginDTO userLoginDTO) {
        return new AuthenticationResponse(userService.login(userLoginDTO));
    }

}
