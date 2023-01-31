package com.trivialware.hotelms.Controllers;

import com.trivialware.hotelms.Entities.Hotel;
import com.trivialware.hotelms.Entities.Room;
import com.trivialware.hotelms.Entities.User;
import com.trivialware.hotelms.Enums.UserRole;
import com.trivialware.hotelms.Exceptions.CustomException;
import com.trivialware.hotelms.Models.Hotel.*;
import com.trivialware.hotelms.Services.HotelService;
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

@Tag(name = "Hotels")
@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    private final ModelMapper mapper;


    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @PostMapping()
    HotelDTOAdmin createHotel(@RequestBody @Valid HotelCreateDTO hotelCreateDTO) {
        return mapper.map(hotelService.addHotel(hotelCreateDTO), HotelDTOAdmin.class);
    }

    @GetMapping()
    List<HotelDTOSimple> getHotels(@AuthenticationPrincipal User user) {
        List<Hotel> hotels = hotelService.getHotels();
        if (user.getRole() == UserRole.ADMINISTRATOR) {
            return hotels.stream().map(hotel -> mapper.map(hotel, HotelDTOSimpleAdmin.class)).collect(Collectors.toList());
        }
        hotels.forEach(hotel -> hotel.setRooms(hotel.getRooms().stream().filter(Room::isActive).collect(Collectors.toList())));
        return hotels.stream().filter(Hotel::isActive).map(hotel -> mapper.map(hotel, HotelDTOSimple.class)).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    HotelDTO getHotel(@PathVariable Long id, @AuthenticationPrincipal User user) {
        Hotel hotel = hotelService.getById(id);
        if (user.getRole() != UserRole.ADMINISTRATOR) {
            if (!hotel.isActive()) {
                throw new CustomException("No Hotel with Such ID", HttpStatus.NOT_FOUND);
            }
            hotel.setRooms(hotel.getRooms().stream().filter(Room::isActive).collect(Collectors.toList()));
            return mapper.map(hotel, HotelDTO.class);
        }
        return mapper.map(hotel, HotelDTOAdmin.class);
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @PutMapping("/{id}")
    HotelDTOAdmin updateHotel(@RequestBody @Valid HotelUpdateDTO updateModel, @PathVariable Long id) {
        Hotel hotel = hotelService.getById(id);
        return mapper.map(hotelService.updateHotel(hotel, updateModel), HotelDTOAdmin.class);
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @DeleteMapping("/{id}")
    void deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotel(hotelService.getById(id));
    }
}
