package com.trivialware.hotelms.Controllers;

import com.trivialware.hotelms.Entities.Room;
import com.trivialware.hotelms.Entities.User;
import com.trivialware.hotelms.Enums.UserRole;
import com.trivialware.hotelms.Exceptions.CustomException;
import com.trivialware.hotelms.Models.Room.RoomCreateDTO;
import com.trivialware.hotelms.Models.Room.RoomDTO;
import com.trivialware.hotelms.Models.Room.RoomDTOAdmin;
import com.trivialware.hotelms.Models.Room.RoomUpdateDTO;
import com.trivialware.hotelms.Services.RoomService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Rooms")
@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;
    private final ModelMapper mapper;

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @PostMapping()
    RoomDTOAdmin createRoom(@RequestBody @Valid RoomCreateDTO roomCreateDTO) {
        return mapper.map(roomService.addRoom(roomCreateDTO), RoomDTOAdmin.class);
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @GetMapping()
    List<RoomDTOAdmin> getRooms() {
        return roomService.getRooms().stream().map(room -> mapper.map(room, RoomDTOAdmin.class)).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    RoomDTO getRoom(@PathVariable Long id, @AuthenticationPrincipal User user) {
        Room room = roomService.getById(id);
        if (user.getRole() != UserRole.ADMINISTRATOR) {
            if (!room.isActive() || !room.getHotel().isActive()) {
                throw new CustomException(String.format("No Room with ID %d", id), HttpStatus.NOT_FOUND);
            }
            return mapper.map(room, RoomDTO.class);
        }
        return mapper.map(room, RoomDTOAdmin.class);
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @PutMapping("/{id}")
    RoomDTOAdmin updateRoom(@RequestBody @Valid RoomUpdateDTO roomUpdateDTO, @PathVariable Long id) {
        Room room = roomService.getById(id);
        return mapper.map(roomService.updateRoom(room, roomUpdateDTO), RoomDTOAdmin.class);
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @DeleteMapping("/{id}")
    void deleteRoom(@PathVariable Long id) {
        roomService.removeRoom(roomService.getById(id));
    }
}
