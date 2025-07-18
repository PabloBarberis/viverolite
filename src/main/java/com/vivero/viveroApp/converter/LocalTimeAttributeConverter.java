package com.vivero.viveroApp.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Converter(autoApply = true)
public class LocalTimeAttributeConverter implements AttributeConverter<LocalTime, String> {

    // Formato esperado en la base de datos
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    // 📤 Convertir de LocalTime a String (para guardar en BD)
    @Override
    public String convertToDatabaseColumn(LocalTime attribute) {
        return attribute != null ? attribute.format(formatter) : null;
    }

    // 📥 Convertir de String a LocalTime (para usar en Java)
    @Override
    public LocalTime convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalTime.parse(dbData, formatter);
        } catch (DateTimeParseException e) {
            // Opcional: loguear el error
            System.err.println("Error al parsear hora desde la base de datos: " + dbData);
            return null;
        }
    }
}
