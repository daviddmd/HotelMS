package com.trivialware.hotelms.Models.User;

import com.trivialware.hotelms.Enums.UserRole;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserDTOAdmin extends UserDTO {
    private boolean enabled;
    private UserRole role;
}
