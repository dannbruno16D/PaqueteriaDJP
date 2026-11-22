package com.paqueteria.djp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "unidades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Unidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String codigoUnidad;

    @Column(nullable = false, length = 80)
    private String descripcion;

    @Column(length = 25)
    private String placas;

    @Column(nullable = false)
    private Double capacidadKg;

    @Builder.Default
    @Column(nullable = false)
    private boolean activa = true;

    @PrePersist
    @PreUpdate
    protected void normalizar() {
        if (this.codigoUnidad != null) {
            this.codigoUnidad = this.codigoUnidad.trim().toUpperCase();
        }
        if (this.descripcion != null) {
            this.descripcion = this.descripcion.trim();
        }
        if (this.placas != null) {
            this.placas = this.placas.trim().toUpperCase();
        }
        if (this.capacidadKg == null || this.capacidadKg <= 0) {
            this.capacidadKg = 500.0;
        }
    }
}
