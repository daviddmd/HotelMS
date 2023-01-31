package com.trivialware.hotelms.Models.ServiceHotel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceHotelDTO {
    private Long id;
    private String name;
    private String description;
    private String code;
    private BigDecimal cost;
    private Long hotelId;
}
