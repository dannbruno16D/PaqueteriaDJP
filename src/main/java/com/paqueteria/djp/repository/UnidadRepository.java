

package com.paqueteria.djp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paqueteria.djp.model.Unidad;

public interface UnidadRepository extends JpaRepository<Unidad, Long> {

    Optional<Unidad> findByCodigoUnidadIgnoreCase(String codigoUnidad);

    boolean existsByCodigoUnidadIgnoreCase(String codigoUnidad);

    List<Unidad> findByActivaTrueOrderByCodigoUnidadAsc();
}