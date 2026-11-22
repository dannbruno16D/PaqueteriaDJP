package com.paqueteria.djp.repository;

import com.paqueteria.djp.model.EstadoPaquete;
import com.paqueteria.djp.model.Paquete;
import com.paqueteria.djp.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaqueteRepository extends JpaRepository<Paquete, Long> {
    Optional<Paquete> findByCodigoRastreo(String codigoRastreo);
    boolean existsByCodigoRastreo(String codigoRastreo);
    List<Paquete> findByEstado(EstadoPaquete estado);
    List<Paquete> findByTransportista(Usuario transportista);
}