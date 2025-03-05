package com.vivero.viveroApp.model.enums;

public enum Descuento {
    SIN_DESCUENTO(0, "0% Descuento"),
    DESCUENTO_5(5, "5% Descuento"),
    DESCUENTO_10(10, "10% Descuento"),
    DESCUENTO_15(15, "15% Descuento"),
    DESCUENTO_20(20, "20% Descuento"),
    DESCUENTO_25(25, "25% Descuento"),
    DESCUENTO_30(30, "30% Descuento"),
    DESCUENTO_35(35, "35% Descuento"),
    DESCUENTO_40(40, "40% Descuento"),
    DESCUENTO_45(45, "45% Descuento"),
    DESCUENTO_50(50, "50% Descuento");

    private final int porcentaje;
    private final String descripcion;

    Descuento(int porcentaje, String descripcion) {
        this.porcentaje = porcentaje;
        this.descripcion = descripcion;
    }

    public int getPorcentaje() {
        return porcentaje;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
