package com.paqueteria.djp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paqueteria.djp.model.Rol;
import com.paqueteria.djp.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByUsernameIgnoreCase(String username);

    boolean existsByUsername(String username);

    boolean existsByUsernameIgnoreCase(String username);

    List<Usuario> findByRol(Rol rol);

    List<Usuario> findByRolAndActivoTrueOrderByNombreAsc(Rol rol);

    List<Usuario> findAllByOrderByNombreAsc();
}
