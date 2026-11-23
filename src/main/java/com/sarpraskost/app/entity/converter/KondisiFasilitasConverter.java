package com.sarpraskost.app.entity.converter;

import com.sarpraskost.app.entity.KondisiFasilitas;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class KondisiFasilitasConverter implements AttributeConverter<KondisiFasilitas, String> {

    @Override
    public String convertToDatabaseColumn(KondisiFasilitas attribute) {
        return attribute != null ? attribute.getDbValue() : null;
    }

    @Override
    public KondisiFasilitas convertToEntityAttribute(String dbData) {
        return dbData != null ? KondisiFasilitas.fromInput(dbData) : null;
    }
}
