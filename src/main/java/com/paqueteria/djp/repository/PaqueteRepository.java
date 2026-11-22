package com.paqueteria.djp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paqueteria.djp.model.EstadoPaquete;
import com.paqueteria.djp.model.Paquete;
import com.paqueteria.djp.model.Usuario;

public interface PaqueteRepository extends JpaRepository<Paquete, Long> {

    Optional<Paquete> findByCodigoRastreo(String codigoRastreo);

    Optional<Paquete> findByCodigoRastreoIgnoreCase(String codigoRastreo);

    boolean existsByCodigoRastreo(String codigoRastreo);

    List<Paquete> findByEstado(EstadoPaquete estado);

    List<Paquete> findAllByOrderByUltimaActualizacionDesc();

    List<Paquete> findByTransportista(Usuario transportista);

    List<Paquete> findByTransportistaIdOrderByUltimaActualizacionDesc(Long transportistaId);

    long countByTransportistaIdAndEstadoIn(Long transportistaId, List<EstadoPaquete> estados);
}