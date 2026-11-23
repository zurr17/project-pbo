package com.sarpraskost.app.entity.converter;

import com.sarpraskost.app.entity.StatusHunian;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class StatusHunianConverter implements AttributeConverter<StatusHunian, String> {

    @Override
    public String convertToDatabaseColumn(StatusHunian attribute) {
        return attribute != null ? attribute.getDbValue() : null;
    }

    @Override
    public StatusHunian convertToEntityAttribute(String dbData) {
        return dbData != null ? StatusHunian.fromInput(dbData) : null;
    }
}
