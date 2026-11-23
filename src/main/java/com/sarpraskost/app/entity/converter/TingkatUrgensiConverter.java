package com.sarpraskost.app.entity.converter;

import com.sarpraskost.app.entity.TingkatUrgensi;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class TingkatUrgensiConverter implements AttributeConverter<TingkatUrgensi, String> {

    @Override
    public String convertToDatabaseColumn(TingkatUrgensi attribute) {
        return attribute != null ? attribute.getDbValue() : null;
    }

    @Override
    public TingkatUrgensi convertToEntityAttribute(String dbData) {
        return dbData != null ? TingkatUrgensi.fromInput(dbData) : null;
    }
}
