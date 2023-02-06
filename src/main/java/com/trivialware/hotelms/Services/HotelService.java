package com.trivialware.hotelms.Services;

import com.trivialware.hotelms.Entities.Hotel;
import com.trivialware.hotelms.Exceptions.CustomException;
import com.trivialware.hotelms.Models.Hotel.HotelCreateDTO;
import com.trivialware.hotelms.Models.Hotel.HotelUpdateDTO;
import com.trivialware.hotelms.Repositories.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HotelService {
    private final HotelRepository hotelRepository;

    public Hotel getById(Long id) {
        return hotelRepository.findById(id).orElseThrow(() -> new CustomException(String.format("No Hotel with ID %d", id), HttpStatus.NOT_FOUND));
    }

    public Hotel updateHotel(Hotel hotel, HotelUpdateDTO updateModel) {
        hotel.setName(updateModel.getName());
        hotel.setDescription(updateModel.getDescription());
        hotel.setCode(updateModel.getCode());
        hotel.setActive(updateModel.isActive());
        return hotelRepository.save(hotel);
    }

    public List<Hotel> getHotels() {
        return hotelRepository.findAll();
    }

    public Hotel addHotel(HotelCreateDTO hotelCreateDTO) {
        Hotel hotel = Hotel.builder().
                name(hotelCreateDTO.getName()).
                code(hotelCreateDTO.getCode()).
                description(hotelCreateDTO.getDescription()).
                active(true).
                services(List.of()).
                rooms(List.of()).
                build();
        if (hotelRepository.existsByCodeIgnoreCase(hotel.getCode())) {
            throw new CustomException(String.format("A Hotel with the same code (%s) already exists.", hotel.getCode()), HttpStatus.BAD_REQUEST);
        }
        return hotelRepository.save(hotel);
    }

    public void deleteHotel(Hotel hotel) {
        hotelRepository.delete(hotel);
    }
}
