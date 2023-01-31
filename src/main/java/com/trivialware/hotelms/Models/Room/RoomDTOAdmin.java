package com.trivialware.hotelms.Models.Room;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RoomDTOAdmin extends RoomDTO {
    private boolean active;
}
