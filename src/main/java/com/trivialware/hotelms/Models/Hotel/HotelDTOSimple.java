package com.trivialware.hotelms.Models.Hotel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HotelDTOSimple {
    private Long id;
    private String name;
    private String code;
    private String description;
    private int numberRooms;
    private int numberServices;
}
