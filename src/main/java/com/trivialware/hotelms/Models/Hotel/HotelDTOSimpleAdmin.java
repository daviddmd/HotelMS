package com.trivialware.hotelms.Models.Hotel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class HotelDTOSimpleAdmin extends HotelDTOSimple {
    private boolean active;
}
