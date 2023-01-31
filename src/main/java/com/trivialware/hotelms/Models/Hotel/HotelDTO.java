package com.trivialware.hotelms.Models.Hotel;

import com.trivialware.hotelms.Models.Room.RoomDTO;
import com.trivialware.hotelms.Models.ServiceHotel.ServiceHotelDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HotelDTO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private List<RoomDTO> rooms;
    private List<ServiceHotelDTO> services;
}
