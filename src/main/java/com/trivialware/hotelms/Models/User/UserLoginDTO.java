package com.trivialware.hotelms.Models.User;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLoginDTO {
    @NotNull
    @NotEmpty
    private String usernameOrEmail;
    @NotNull
    @NotEmpty
    private String password;
    private boolean rememberMe;
}
