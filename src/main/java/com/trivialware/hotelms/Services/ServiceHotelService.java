package com.trivialware.hotelms.Services;

import com.trivialware.hotelms.Entities.Hotel;
import com.trivialware.hotelms.Entities.ServiceHotel;
import com.trivialware.hotelms.Exceptions.CustomException;
import com.trivialware.hotelms.Models.ServiceHotel.ServiceHotelCreateDTO;
import com.trivialware.hotelms.Models.ServiceHotel.ServiceHotelUpdateDTO;
import com.trivialware.hotelms.Repositories.ServiceHotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceHotelService {
    private final ServiceHotelRepository serviceHotelRepository;
    private final HotelService hotelService;

    public ServiceHotel getById(Long id) {
        return serviceHotelRepository.findById(id).orElseThrow(() ->
                new CustomException(String.format("No Hotel Service with ID %d exists.", id), HttpStatus.NOT_FOUND));
    }

    public List<ServiceHotel> getAll() {
        return serviceHotelRepository.findAll();
    }

    public ServiceHotel addHotelService(ServiceHotelCreateDTO dto) {
        Hotel hotel = hotelService.getById(dto.getHotelId());
        if (serviceHotelRepository.existsByHotel_IdAndCodeIgnoreCase(hotel.getId(), dto.getCode())) {
            throw new CustomException(
                    String.format("A Service with the code %s already exists in the Hotel with code %s.", dto.getCode(), hotel.getCode()),
                    HttpStatus.BAD_REQUEST
            );
        }
        ServiceHotel service = ServiceHotel.builder().
                name(dto.getName()).
                description(dto.getDescription()).
                code(dto.getCode()).
                cost(dto.getCost()).
                hotel(hotel).
                build();
        return serviceHotelRepository.save(service);
    }

    public ServiceHotel updateHotelService(ServiceHotel service, ServiceHotelUpdateDTO dto) {
        if (!service.getCode().equals(dto.getCode()) && serviceHotelRepository.existsByHotel_IdAndCodeIgnoreCase(service.getHotel().getId(), dto.getCode())) {
            throw new CustomException(
                    String.format("A Service with the code %s already exists in the Hotel with code %s.", dto.getCode(), service.getHotel().getCode()),
                    HttpStatus.BAD_REQUEST
            );
        }
        service.setName(dto.getName());
        service.setDescription(dto.getDescription());
        service.setCode(dto.getCode());
        service.setCost(dto.getCost());
        return serviceHotelRepository.save(service);
    }

    public void deleteHotelService(ServiceHotel service) {
        serviceHotelRepository.delete(service);
    }

}
