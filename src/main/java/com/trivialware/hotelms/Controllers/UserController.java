package com.trivialware.hotelms.Controllers;

import com.trivialware.hotelms.Entities.User;
import com.trivialware.hotelms.Models.User.UserDTO;
import com.trivialware.hotelms.Models.User.UserDTOAdmin;
import com.trivialware.hotelms.Models.User.UserUpdateAdminDTO;
import com.trivialware.hotelms.Models.User.UserUpdateDTO;
import com.trivialware.hotelms.Services.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Users")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final ModelMapper mapper;

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @GetMapping
    List<UserDTOAdmin> getAllUsers() {
        return userService.getUsers().stream().map(user -> mapper.map(user, UserDTOAdmin.class)).collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @GetMapping("/{id}")
    UserDTOAdmin getUserById(@PathVariable Long id) {
        return mapper.map(userService.getUserById(id), UserDTOAdmin.class);
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @PutMapping("/{id}")
    UserDTOAdmin updateUserById(@PathVariable Long id, @RequestBody @Valid UserUpdateAdminDTO userUpdateAdminDTO) {
        return mapper.map(userService.updateUserAdmin(userService.getUserById(id), userUpdateAdminDTO), UserDTOAdmin.class);
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @DeleteMapping("/{id}")
    void deleteUserById(@PathVariable Long id) {
        userService.delete(userService.getUserById(id));
    }

    @GetMapping("self")
    UserDTO getSelf(@AuthenticationPrincipal User user) {
        return mapper.map(userService.getUserById(user.getId()), UserDTO.class);
    }

    @PutMapping("self")
    UserDTO updateSelf(@AuthenticationPrincipal User user, @RequestBody @Valid UserUpdateDTO userUpdateDTO) {
        return mapper.map(userService.updateSelf(userService.getUserById(user.getId()), userUpdateDTO), UserDTO.class);
    }

    @DeleteMapping("self")
    void deactivateSelf(@AuthenticationPrincipal User user) {
        userService.deactivateUser(user);
    }

}
