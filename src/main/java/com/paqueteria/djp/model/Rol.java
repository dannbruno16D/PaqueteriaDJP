package com.paqueteria.djp.model;

public enum Rol {
    ADMINISTRADOR("Administrador"),
    RECEPCION("Recepción"),
    ALMACEN("Almacén"),
    TRANSPORTISTA("Transportista");

    private final String etiqueta;

    Rol(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}