package com.trivialware.hotelms.Services;

import com.trivialware.hotelms.Entities.RoomType;
import com.trivialware.hotelms.Exceptions.CustomException;
import com.trivialware.hotelms.Models.RoomType.RoomTypeCreateDTO;
import com.trivialware.hotelms.Models.RoomType.RoomTypeUpdateDTO;
import com.trivialware.hotelms.Repositories.RoomRepository;
import com.trivialware.hotelms.Repositories.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomTypeService {
    private final RoomTypeRepository roomTypeRepository;

    private final RoomRepository roomRepository;

    public RoomType getById(Long id) {
        return roomTypeRepository.findById(id).orElseThrow(
                () -> new CustomException(String.format("No Room Type with ID %d", id), HttpStatus.NOT_FOUND)
        );
    }

    public RoomType updateRoomType(RoomType roomType, RoomTypeUpdateDTO roomTypeUpdateDTO) {
        if (!roomType.getName().equals(roomTypeUpdateDTO.getName()) && roomTypeRepository.existsByNameIgnoreCase(roomTypeUpdateDTO.getName())) {
            throw new CustomException(String.format("A Room Type with the name %s already exists.", roomTypeUpdateDTO.getName()), HttpStatus.BAD_REQUEST);
        }
        roomType.setName(roomTypeUpdateDTO.getName());
        roomType.setBasePrice(roomTypeUpdateDTO.getBasePrice());
        roomType.setMaximumCapacity(roomTypeUpdateDTO.getMaximumCapacity());
        return roomTypeRepository.save(roomType);
    }

    public List<RoomType> getRoomTypes() {
        return roomTypeRepository.findAll();
    }

    public RoomType addRoomType(RoomTypeCreateDTO roomTypeCreateDTO) {
        RoomType roomType = RoomType.builder().
                name(roomTypeCreateDTO.getName()).
                basePrice(roomTypeCreateDTO.getBasePrice().setScale(2, RoundingMode.HALF_EVEN)).
                maximumCapacity(roomTypeCreateDTO.getMaximumCapacity()).build();
        if (roomTypeRepository.existsByNameIgnoreCase(roomTypeCreateDTO.getName())) {
            throw new CustomException(String.format("A Room Type with the name %s already exists.", roomTypeCreateDTO.getName()), HttpStatus.BAD_REQUEST);
        }
        return roomTypeRepository.save(roomType);
    }

    public void deleteRoomType(RoomType roomType) {
        if (!roomRepository.findByRoomType(roomType).isEmpty()) {
            throw new CustomException("Can't delete room type that's in use by one or more rooms.", HttpStatus.BAD_REQUEST);
        }
        roomTypeRepository.delete(roomType);
    }
}
