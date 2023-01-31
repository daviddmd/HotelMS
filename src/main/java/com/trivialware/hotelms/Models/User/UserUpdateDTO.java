package com.trivialware.hotelms.Models.User;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UserUpdateDTO {
    @NotNull
    @NotEmpty
    private String first_name;
    @NotNull
    @NotEmpty
    private String last_name;
    @NotNull
    @NotEmpty
    private String email;
    private String phoneNumber;

    private String currentPassword;

    private String newPassword;
}
