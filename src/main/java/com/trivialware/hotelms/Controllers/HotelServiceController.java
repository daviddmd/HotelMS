package com.trivialware.hotelms.Controllers;

import com.trivialware.hotelms.Models.ServiceHotel.ServiceHotelCreateDTO;
import com.trivialware.hotelms.Models.ServiceHotel.ServiceHotelDTO;
import com.trivialware.hotelms.Models.ServiceHotel.ServiceHotelUpdateDTO;
import com.trivialware.hotelms.Services.ServiceHotelService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Hotel Services")
@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMINISTRATOR')")
public class HotelServiceController {
    private final ServiceHotelService serviceHotelService;
    private final ModelMapper mapper;

    @PostMapping()
    ServiceHotelDTO createHotelService(@RequestBody @Valid ServiceHotelCreateDTO dto) {
        return mapper.map(serviceHotelService.addHotelService(dto), ServiceHotelDTO.class);
    }

    @GetMapping
    List<ServiceHotelDTO> getHotelServices() {
        return serviceHotelService.getAll().stream().map(service -> mapper.map(service, ServiceHotelDTO.class)).toList();
    }

    @GetMapping("/{id}")
    ServiceHotelDTO getHotelService(@PathVariable Long id) {
        return mapper.map(serviceHotelService.getById(id), ServiceHotelDTO.class);
    }

    @PutMapping("/{id}")
    ServiceHotelDTO updateHotelService(@PathVariable Long id, @RequestBody @Valid ServiceHotelUpdateDTO dto) {
        return mapper.map(serviceHotelService.updateHotelService(serviceHotelService.getById(id), dto), ServiceHotelDTO.class);
    }

    @DeleteMapping("/{id}")
    void deleteHotelService(@PathVariable Long id) {
        serviceHotelService.deleteHotelService(serviceHotelService.getById(id));
    }
}
