package com.sarpraskost.app.entity.converter;

import com.sarpraskost.app.entity.Role;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class RoleConverter implements AttributeConverter<Role, String> {

    @Override
    public String convertToDatabaseColumn(Role attribute) {
        return attribute != null ? attribute.getDbValue() : null;
    }

    @Override
    public Role convertToEntityAttribute(String dbData) {
        return dbData != null ? Role.fromInput(dbData) : null;
    }
}
