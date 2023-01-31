package com.trivialware.hotelms.Models.Mapper;

import org.modelmapper.AbstractConverter;

import java.util.List;

public class ListToListCountConverter<T> extends AbstractConverter<List<T>, Integer> {
    @Override
    protected Integer convert(List<T> source) {
        return source == null ? 0 : source.size();
    }
}
