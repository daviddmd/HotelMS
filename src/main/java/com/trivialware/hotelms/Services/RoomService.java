package com.trivialware.hotelms.Services;

import com.trivialware.hotelms.Entities.Hotel;
import com.trivialware.hotelms.Entities.Room;
import com.trivialware.hotelms.Entities.RoomType;
import com.trivialware.hotelms.Exceptions.CustomException;
import com.trivialware.hotelms.Models.Room.RoomCreateDTO;
import com.trivialware.hotelms.Models.Room.RoomUpdateDTO;
import com.trivialware.hotelms.Repositories.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;

    private final HotelService hotelService;

    private final RoomTypeService roomTypeService;

    public Room getById(Long id) {
        return roomRepository.findById(id).orElseThrow(() -> new CustomException(String.format("No Room with ID %d.", id), HttpStatus.NOT_FOUND));
    }

    public Room updateRoom(Room room, RoomUpdateDTO roomUpdateDTO) {
        if (!room.getCode().equals(roomUpdateDTO.getCode()) && roomRepository.existsByCodeIgnoreCaseAndHotel_Id(roomUpdateDTO.getCode(), room.getHotel().getId())) {
            throw new CustomException(
                    String.format("A Room with the code %s already exists in the Hotel with code %s.", roomUpdateDTO.getCode(), room.getHotel().getCode()),
                    HttpStatus.BAD_REQUEST
            );
        }
        RoomType roomType = roomTypeService.getById(roomUpdateDTO.getRoomTypeID());
        room.setCode(roomUpdateDTO.getCode());
        room.setName(roomUpdateDTO.getName());
        room.setDescription(roomUpdateDTO.getDescription());
        room.setRoomType(roomType);
        room.setPrice(roomUpdateDTO.getPrice().setScale(2, RoundingMode.HALF_UP));
        room.setActive(roomUpdateDTO.isActive());
        return roomRepository.save(room);
    }

    public List<Room> getRooms() {
        return roomRepository.findAll();
    }

    public Room addRoom(RoomCreateDTO roomCreateDTO) {
        Hotel hotel = hotelService.getById(roomCreateDTO.getHotelID());
        if (roomRepository.existsByCodeIgnoreCaseAndHotel_Id(roomCreateDTO.getCode(), hotel.getId())) {
            throw new CustomException(
                    String.format("A Room with the code %s already exists in the Hotel with code %s.", roomCreateDTO.getCode(), hotel.getCode()),
                    HttpStatus.BAD_REQUEST
            );
        }
        RoomType roomType = roomTypeService.getById(roomCreateDTO.getRoomTypeID());
        Room room = Room.builder().
                name(roomCreateDTO.getName()).
                code(roomCreateDTO.getCode()).
                description(roomCreateDTO.getDescription()).
                roomType(roomType).
                price(roomCreateDTO.getPrice().setScale(2, RoundingMode.HALF_UP)).
                active(true).
                hotel(hotel).
                build();
        return roomRepository.save(room);
    }

    public void removeRoom(Room room) {
        roomRepository.delete(room);
    }

}
