package com.trivialware.hotelms.Exceptions;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ExceptionResponse {
    private final String message;
}
