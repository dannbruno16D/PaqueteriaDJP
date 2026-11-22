package com.paqueteria.djp.model;

public enum EstadoPaquete {
    REGISTRADO(
            "Registrado",
            "Orden de envío creada y paquete recibido en sucursal."
    ),
    EN_ALMACEN(
            "En almacén",
            "Paquete verificado y clasificado en centro de distribución."
    ),
    EN_TRANSITO(
            "En tránsito",
            "El envío va en ruta hacia la ciudad de destino."
    ),
    EN_REPARTO(
            "En reparto",
            "¡Ya casi llega! El repartidor va en camino a tu domicilio."
    ),
    ENTREGADO(
            "Entregado",
            "Su envío ha sido entregado con éxito al destinatario."
    ),
    INCIDENCIA(
            "Incidencia",
            "Se presentó un inconveniente durante el traslado o entrega."
    );

    private final String etiqueta;
    private final String mensajePorDefecto;

    EstadoPaquete(String etiqueta, String mensajePorDefecto) {
        this.etiqueta = etiqueta;
        this.mensajePorDefecto = mensajePorDefecto;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getMensajePorDefecto() {
        return mensajePorDefecto;
    }
}
