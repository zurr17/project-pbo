package com.sarpraskost.app.entity.converter;

import com.sarpraskost.app.entity.StatusTiket;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class StatusTiketConverter implements AttributeConverter<StatusTiket, String> {

    @Override
    public String convertToDatabaseColumn(StatusTiket attribute) {
        return attribute != null ? attribute.getDbValue() : null;
    }

    @Override
    public StatusTiket convertToEntityAttribute(String dbData) {
        return dbData != null ? StatusTiket.fromInput(dbData) : null;
    }
}
