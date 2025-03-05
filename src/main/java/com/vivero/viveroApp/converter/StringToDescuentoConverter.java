package com.vivero.viveroApp.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import com.vivero.viveroApp.model.enums.Descuento;

@Component
public class StringToDescuentoConverter implements Converter<String, Descuento> {
    @Override
    public Descuento convert(String source) {
        try {
            int porcentaje = Integer.parseInt(source);
            for (Descuento d : Descuento.values()) {
                if (d.getPorcentaje() == porcentaje) {
                    return d;
                }
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid descuento value: " + source);
        }
        throw new IllegalArgumentException("No enum constant for value: " + source);
    }
}
