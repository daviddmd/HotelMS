package com.trivialware.hotelms.Models.RoomType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder
public class RoomTypeUpdateDTO {
    @NotEmpty
    @NotNull
    private String name;
    @NotNull
    @Min(1)
    private int maximumCapacity;
    @NotNull
    @Min(0)
    private BigDecimal basePrice;
}
