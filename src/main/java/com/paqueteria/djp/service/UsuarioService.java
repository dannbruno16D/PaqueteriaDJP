package com.paqueteria.djp.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paqueteria.djp.model.EstadoPaquete;
import com.paqueteria.djp.model.Rol;
import com.paqueteria.djp.model.Usuario;
import com.paqueteria.djp.repository.PaqueteRepository;
import com.paqueteria.djp.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PaqueteRepository paqueteRepository;

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAllByOrderByNombreAsc();
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> buscarPorUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        return usuarioRepository.findByUsernameIgnoreCase(username.trim());
    }

    public List<Usuario> listarPorRol(Rol rol) {
        return usuarioRepository.findByRol(rol);
    }

    public List<Usuario> listarTransportistasActivos() {
        return usuarioRepository.findByRolAndActivoTrueOrderByNombreAsc(Rol.TRANSPORTISTA);
    }

    @Transactional
    public Usuario guardar(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Los datos del usuario son obligatorios.");
        }

        String nombreLimpio = usuario.getNombre() == null ? "" : usuario.getNombre().trim();
        if (nombreLimpio.length() < 3) {
            throw new IllegalArgumentException("El nombre del usuario debe tener al menos 3 caracteres.");
        }
        usuario.setNombre(nombreLimpio);

        String usernameLimpio = usuario.getUsername() == null ? "" : usuario.getUsername().trim().toLowerCase();
        if (!usernameLimpio.matches("^[a-z0-9._-]{3,50}$")) {
            throw new IllegalArgumentException(
                    "El usuario debe tener entre 3 y 50 caracteres (solo letras, números, punto, guion o guion bajo, sin espacios)."
            );
        }
        usuario.setUsername(usernameLimpio);

        Optional<Usuario> existentePorUsername = usuarioRepository.findByUsernameIgnoreCase(usernameLimpio);
        if (existentePorUsername.isPresent()
                && (usuario.getId() == null || !existentePorUsername.get().getId().equals(usuario.getId()))) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con el nombre de usuario '" + usernameLimpio + "'.");
        }

        if (usuario.getId() == null) {
            String passLimpia = usuario.getPassword() == null ? "" : usuario.getPassword().trim();
            if (passLimpia.length() < 4) {
                throw new IllegalArgumentException("La contraseña debe tener al menos 4 caracteres.");
            }
            usuario.setPassword(passLimpia);
        } else {
            Usuario actual = usuarioRepository.findById(usuario.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id: " + usuario.getId()));
            if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
                usuario.setPassword(actual.getPassword());
            } else if (usuario.getPassword().trim().length() < 4) {
                throw new IllegalArgumentException("La nueva contraseña debe tener al menos 4 caracteres.");
            } else {
                usuario.setPassword(usuario.getPassword().trim());
            }
        }

        if (usuario.getRol() == null) {
            usuario.setRol(Rol.RECEPCION);
        }

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario cambiarEstadoActivo(Long id, boolean activo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id: " + id));

        if (!activo && usuario.getRol() == Rol.TRANSPORTISTA) {
            long paquetesActivos = paqueteRepository.countByTransportistaIdAndEstadoIn(
                    usuario.getId(),
                    List.of(EstadoPaquete.EN_TRANSITO, EstadoPaquete.EN_REPARTO)
            );
            if (paquetesActivos > 0) {
                throw new IllegalStateException(
                        "No se puede desactivar a " + usuario.getNombre()
                                + " porque tiene " + paquetesActivos + " paquete(s) en tránsito o en reparto."
                );
            }
        }

        usuario.setActivo(activo);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id: " + id));

        if (!paqueteRepository.findByTransportistaIdOrderByUltimaActualizacionDesc(id).isEmpty()) {
            throw new IllegalStateException(
                    "No se puede eliminar a " + usuario.getNombre()
                            + " porque tiene paquetes asociados en el historial. Mejor desactívalo."
            );
        }

        usuarioRepository.deleteById(id);
    }
}