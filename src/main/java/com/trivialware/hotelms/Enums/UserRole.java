package com.trivialware.hotelms.Enums;

import org.springframework.security.core.GrantedAuthority;

public enum UserRole implements GrantedAuthority {
    CUSTOMER,
    EMPLOYEE,
    ADMINISTRATOR;

    @Override
    public String getAuthority() {
        return name();
    }
}
