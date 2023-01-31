package com.trivialware.hotelms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MvcResult;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class Helper {
    public static final String usernameAdmin = "admin";
    public static final String passwordAdmin = "admin789";
    public static final String firstNameAdmin = "David";
    public static final String lastNameAdmin = "Duarte";
    public static final String emailAdmin = "david.duarte@example.com";
    public static final String phoneNumberAdmin = "910000000";


    public static final String usernameEmployee = "employee";
    public static final String passwordEmployee = "employee456";
    public static final String firstNameEmployee = "Pedro";
    public static final String lastNameEmployee = "Santos";
    public static final String emailEmployee = "pedro.santos@example.com";
    public static final String phoneNumberEmployee = "920000000";

    public static final String usernameFirstCustomer = "customer1";
    public static final String passwordFirstCustomer = "customer123";
    public static final String firstNameFirstCustomer = "Carlos";
    public static final String lastNameFirstCustomer = "Sousa";
    public static final String emailFirstCustomer = "carlos.sousa@example.com";
    public static final String phoneNumberFirstCustomer = "930000000";

    public static final String usernameSecondCustomer = "customer2";
    public static final String passwordSecondCustomer = "customer456";
    public static final String firstNameSecondCustomer = "João";
    public static final String lastNameSecondCustomer = "Félis";
    public static final String emailSecondCustomer = "joao.felis@example.com";
    public static final String phoneNumberSecondCustomer = "940000000";

    public static <T> String toJson(ObjectMapper objectMapper, T object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    public static <T> T fromJson(ObjectMapper mapper, MvcResult result, Class<T> tClass) throws JsonProcessingException, UnsupportedEncodingException {
        return mapper.readValue(result.getResponse().getContentAsString(StandardCharsets.UTF_8), tClass);
    }

    public static <T> List<T> listFromJson(ObjectMapper mapper, MvcResult result, Class<T[]> tClass) throws JsonProcessingException, UnsupportedEncodingException {
        return Arrays.asList(mapper.readValue(result.getResponse().getContentAsString(), tClass));
    }
}
