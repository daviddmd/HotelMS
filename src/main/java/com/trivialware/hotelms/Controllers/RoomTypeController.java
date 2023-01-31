package com.trivialware.hotelms.Controllers;

import com.trivialware.hotelms.Entities.RoomType;
import com.trivialware.hotelms.Models.RoomType.RoomTypeCreateDTO;
import com.trivialware.hotelms.Models.RoomType.RoomTypeDTO;
import com.trivialware.hotelms.Models.RoomType.RoomTypeUpdateDTO;
import com.trivialware.hotelms.Services.RoomTypeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Tag(name = "Room Types")
@RestController
@RequestMapping("/room-types")
@RequiredArgsConstructor
//@Secured("ADMINISTRATOR")
@PreAuthorize("hasAuthority('ADMINISTRATOR')")
public class RoomTypeController {
    private final RoomTypeService roomTypeService;
    private final ModelMapper mapper;

    @PostMapping()
    RoomTypeDTO createRoomType(@RequestBody @Valid RoomTypeCreateDTO roomTypeCreateDTO) {
        return mapper.map(roomTypeService.addRoomType(roomTypeCreateDTO), RoomTypeDTO.class);
    }

    @GetMapping
    List<RoomTypeDTO> getRoomTypes() {
        return roomTypeService.getRoomTypes().stream().map(roomType -> mapper.map(roomType, RoomTypeDTO.class)).toList();
    }

    @GetMapping("/{id}")
    RoomTypeDTO getRoomType(@PathVariable Long id) {
        RoomType roomType = roomTypeService.getById(id);
        return mapper.map(roomType, RoomTypeDTO.class);
    }

    @PutMapping("/{id}")
    RoomTypeDTO updateRoomType(@PathVariable Long id, @RequestBody @Valid RoomTypeUpdateDTO updateModel) {
        RoomType roomType = roomTypeService.getById(id);
        return mapper.map(roomTypeService.updateRoomType(roomType, updateModel), RoomTypeDTO.class);
    }

    @DeleteMapping("/{id}")
    void deleteRoomType(@PathVariable Long id) {
        RoomType roomType = roomTypeService.getById(id);
        roomTypeService.deleteRoomType(roomType);
    }

}
