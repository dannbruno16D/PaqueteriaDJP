package com.paqueteria.djp.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "paquetes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paquete {

    private static final Set<EstadoPaquete> ESTADOS_REPETIBLES = EnumSet.of(
            EstadoPaquete.EN_TRANSITO,
            EstadoPaquete.EN_REPARTO,
            EstadoPaquete.INCIDENCIA
    );

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String codigoRastreo;

    @Column(nullable = false, length = 120)
    private String remitente;

    @Column(nullable = false, length = 100)
    private String ciudadOrigen;

    @Column(nullable = false, length = 120)
    private String destinatario;

    @Column(nullable = false, length = 15)
    private String telefonoDestinatario;

    @Column(nullable = false, length = 200)
    private String direccionDestino;

    @Column(nullable = false, length = 160)
    private String descripcionContenido;

    @Column(nullable = false)
    private Double pesoKg;

    @Column(nullable = false)
    private Double costoEnvio;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoPaquete estado = EstadoPaquete.REGISTRADO;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(nullable = false)
    private LocalDateTime ultimaActualizacion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "transportista_id")
    private Usuario transportista;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "unidad_id")
    private Unidad unidad;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "paquete_historial_estados", joinColumns = @JoinColumn(name = "paquete_id"))
    private List<RegistroEstado> historialEstados = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        limpiarTextos();
        LocalDateTime ahora = LocalDateTime.now();
        if (this.fechaRegistro == null) {
            this.fechaRegistro = ahora;
        }
        if (this.ultimaActualizacion == null) {
            this.ultimaActualizacion = ahora;
        }
        if (this.estado == null) {
            this.estado = EstadoPaquete.REGISTRADO;
        }
        if (this.pesoKg == null || this.pesoKg <= 0) {
            this.pesoKg = 1.0;
        }
        if (this.costoEnvio == null || this.costoEnvio < 0) {
            this.costoEnvio = calcularCostoBase(this.pesoKg);
        }
        if (this.historialEstados == null) {
            this.historialEstados = new ArrayList<>();
        }
        if (this.historialEstados.isEmpty()) {
            String ubicacionInicial = (this.ciudadOrigen != null && !this.ciudadOrigen.isBlank())
                    ? "Sucursal " + this.ciudadOrigen
                    : "Sucursal de origen";
            this.historialEstados.add(new RegistroEstado(
                    this.estado,
                    this.ultimaActualizacion,
                    ubicacionInicial,
                    this.estado.getMensajePorDefecto()
            ));
        }
    }

    @PreUpdate
    protected void onUpdate() {
        limpiarTextos();
        if (this.ultimaActualizacion == null) {
            this.ultimaActualizacion = LocalDateTime.now();
        }
    }

    private void limpiarTextos() {
        if (this.codigoRastreo != null) {
            this.codigoRastreo = this.codigoRastreo.trim().toUpperCase();
        }
        if (this.remitente != null) {
            this.remitente = this.remitente.trim();
        }
        if (this.ciudadOrigen != null) {
            this.ciudadOrigen = this.ciudadOrigen.trim();
        }
        if (this.destinatario != null) {
            this.destinatario = this.destinatario.trim();
        }
        if (this.telefonoDestinatario != null) {
            this.telefonoDestinatario = this.telefonoDestinatario.replaceAll("\\s+", "").trim();
        }
        if (this.direccionDestino != null) {
            this.direccionDestino = this.direccionDestino.trim();
        }
        if (this.descripcionContenido != null) {
            this.descripcionContenido = this.descripcionContenido.trim();
        }
    }

    public static double calcularCostoBase(double pesoKg) {
        double pesoSeguro = Math.max(0.5, pesoKg);
        return Math.round((85.0 + (pesoSeguro * 15.0)) * 100.0) / 100.0;
    }

    public void agregarEventoEstado(EstadoPaquete nuevoEstado, LocalDateTime fechaHora, String ubicacion, String detalle) {
        if (nuevoEstado == null) {
            return;
        }
        if (this.historialEstados == null) {
            this.historialEstados = new ArrayList<>();
        }

        LocalDateTime momento = (fechaHora != null) ? fechaHora : LocalDateTime.now();
        String lugarFinal = (ubicacion != null && !ubicacion.isBlank())
                ? ubicacion.trim()
                : resolverUbicacionPorDefecto(nuevoEstado);
        String detalleFinal = (detalle != null && !detalle.isBlank())
                ? detalle.trim()
                : nuevoEstado.getMensajePorDefecto();

        boolean mismoEstado = (this.estado == nuevoEstado);
        boolean esRepetible = ESTADOS_REPETIBLES.contains(nuevoEstado);

        if (mismoEstado && !esRepetible) {
            this.ultimaActualizacion = momento;
            return;
        }

        this.estado = nuevoEstado;
        this.ultimaActualizacion = momento;
        this.historialEstados.add(new RegistroEstado(nuevoEstado, momento, lugarFinal, detalleFinal));
    }

    private String resolverUbicacionPorDefecto(EstadoPaquete estadoObjetivo) {
        return switch (estadoObjetivo) {
            case REGISTRADO -> (this.ciudadOrigen != null && !this.ciudadOrigen.isBlank())
                    ? "Sucursal " + this.ciudadOrigen : "Sucursal de origen";
            case EN_ALMACEN -> "Centro de distribución / Almacén";
            case EN_TRANSITO -> "En ruta entre sucursales";
            case EN_REPARTO -> "Unidad de reparto local";
            case ENTREGADO -> (this.direccionDestino != null && !this.direccionDestino.isBlank())
                    ? this.direccionDestino : "Domicilio del destinatario";
            case INCIDENCIA -> "Seguimiento operativo";
        };
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegistroEstado {

        @Enumerated(EnumType.STRING)
        @Column(name = "estado", nullable = false, length = 30)
        private EstadoPaquete estado;

        @Column(name = "fecha_hora", nullable = false)
        private LocalDateTime fechaHora;

        @Column(name = "ubicacion", length = 160)
        private String ubicacion;

        @Column(name = "detalle", length = 255)
        private String detalle;
    }
}