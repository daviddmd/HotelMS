package com.trivialware.hotelms.Models.User;

import com.trivialware.hotelms.Enums.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateAdminDTO extends UserUpdateDTO {
    @NotNull
    private UserRole role;
    @NotNull
    private boolean enabled;
}
