package com.trivialware.hotelms.Models.Room;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String roomTypeName;
    private int roomTypeCapacity;
    private BigDecimal price;
    private int hotelId;
}
