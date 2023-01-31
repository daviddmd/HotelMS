package com.trivialware.hotelms.Models.RoomType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomTypeDTO {
    private Long id;
    private String name;
    private int maximumCapacity;
    private BigDecimal basePrice;
}
