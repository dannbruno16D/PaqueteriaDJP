package com.paqueteria.djp.service;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paqueteria.djp.model.EstadoPaquete;
import com.paqueteria.djp.model.Paquete;
import com.paqueteria.djp.model.Rol;
import com.paqueteria.djp.model.Unidad;
import com.paqueteria.djp.model.Usuario;
import com.paqueteria.djp.repository.PaqueteRepository;
import com.paqueteria.djp.repository.UnidadRepository;
import com.paqueteria.djp.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaqueteService {

    private static final Set<EstadoPaquete> ESTADOS_REPETIBLES = EnumSet.of(
            EstadoPaquete.EN_TRANSITO,
            EstadoPaquete.EN_REPARTO,
            EstadoPaquete.INCIDENCIA
    );

    private final PaqueteRepository paqueteRepository;
    private final UsuarioRepository usuarioRepository;
    private final UnidadRepository unidadRepository;

    public List<Paquete> listarTodos() {
        return paqueteRepository.findAllByOrderByUltimaActualizacionDesc();
    }

    public Optional<Paquete> buscarPorId(Long id) {
        return paqueteRepository.findById(id);
    }

    public Optional<Paquete> buscarPorCodigoRastreo(String codigoRastreo) {
        if (codigoRastreo == null || codigoRastreo.isBlank()) {
            return Optional.empty();
        }
        return paqueteRepository.findByCodigoRastreoIgnoreCase(codigoRastreo.trim());
    }

    public List<Paquete> listarPorEstado(EstadoPaquete estado) {
        return paqueteRepository.findByEstado(estado);
    }

    public List<Paquete> listarPorTransportista(Usuario transportista) {
        return paqueteRepository.findByTransportista(transportista);
    }

    public List<Paquete> listarPorTransportistaId(Long transportistaId) {
        return paqueteRepository.findByTransportistaIdOrderByUltimaActualizacionDesc(transportistaId);
    }

    @Transactional
    public Paquete registrarPaquete(Paquete paquete) {
        if (paquete == null) {
            throw new IllegalArgumentException("Los datos del paquete son obligatorios.");
        }
        validarCamposRegistro(paquete);

        if (paquete.getCodigoRastreo() == null || paquete.getCodigoRastreo().isBlank()) {
            paquete.setCodigoRastreo(generarCodigoUnico());
        } else {
            String codigoLimpio = paquete.getCodigoRastreo().trim().toUpperCase();
            if (paqueteRepository.existsByCodigoRastreo(codigoLimpio)) {
                throw new IllegalArgumentException("Ya existe un paquete con el código de rastreo: " + codigoLimpio);
            }
            paquete.setCodigoRastreo(codigoLimpio);
        }

        if (paquete.getEstado() == null) {
            paquete.setEstado(EstadoPaquete.REGISTRADO);
        }

        LocalDateTime ahora = LocalDateTime.now();
        paquete.setFechaRegistro(ahora);
        paquete.setUltimaActualizacion(ahora);

        if (paquete.getCostoEnvio() == null || paquete.getCostoEnvio() <= 0) {
            paquete.setCostoEnvio(Paquete.calcularCostoBase(paquete.getPesoKg()));
        }

        return paqueteRepository.save(paquete);
    }

    @Transactional
    public Paquete actualizarEstado(Long id, EstadoPaquete nuevoEstado) {
        return actualizarEstado(id, nuevoEstado, null, null);
    }

    @Transactional
    public Paquete actualizarEstado(Long id, EstadoPaquete nuevoEstado, String ubicacion, String detalle) {
        if (nuevoEstado == null) {
            throw new IllegalArgumentException("Debes seleccionar un estado válido.");
        }

        Paquete paquete = paqueteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paquete no encontrado con id: " + id));

        EstadoPaquete estadoActual = paquete.getEstado();

        if (estadoActual == EstadoPaquete.ENTREGADO) {
            throw new IllegalStateException("El paquete ya fue ENTREGADO y no puede cambiar de estado.");
        }

        if (nuevoEstado == EstadoPaquete.REGISTRADO && estadoActual != EstadoPaquete.REGISTRADO) {
            throw new IllegalStateException("No es posible regresar el paquete al estado REGISTRADO.");
        }

        if (estadoActual == nuevoEstado && !ESTADOS_REPETIBLES.contains(nuevoEstado)) {
            throw new IllegalStateException(
                    "El paquete ya se encuentra en " + nuevoEstado.getEtiqueta() + " y no permite repetir este estado."
            );
        }

        boolean requiereTransportista = nuevoEstado == EstadoPaquete.EN_TRANSITO
                || nuevoEstado == EstadoPaquete.EN_REPARTO
                || nuevoEstado == EstadoPaquete.ENTREGADO;

        if (requiereTransportista) {
            if (paquete.getTransportista() == null) {
                throw new IllegalStateException(
                        "Asigna un transportista antes de cambiar el paquete a " + nuevoEstado.getEtiqueta() + "."
                );
            }
            if (!paquete.getTransportista().isActivo()) {
                throw new IllegalStateException(
                        "El transportista asignado está inactivo. Asigna otro transportista activo."
                );
            }
        }

        paquete.agregarEventoEstado(nuevoEstado, LocalDateTime.now(), ubicacion, detalle);
        return paqueteRepository.save(paquete);
    }

    @Transactional
    public Paquete asignarTransportista(Long id, Usuario transportista) {
        Paquete paquete = paqueteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paquete no encontrado con id: " + id));

        if (paquete.getEstado() == EstadoPaquete.ENTREGADO) {
            throw new IllegalStateException("No se puede reasignar un paquete que ya fue ENTREGADO.");
        }

        if (transportista == null || transportista.getId() == null) {
            throw new IllegalArgumentException("Debes seleccionar un transportista válido.");
        }

        Usuario transportistaReal = usuarioRepository.findById(transportista.getId())
                .orElseThrow(() -> new IllegalArgumentException("Transportista no encontrado."));

        if (transportistaReal.getRol() != Rol.TRANSPORTISTA) {
            throw new IllegalArgumentException("El usuario seleccionado no tiene el rol de TRANSPORTISTA.");
        }

        if (!transportistaReal.isActivo()) {
            throw new IllegalStateException("No se puede asignar un transportista inactivo.");
        }

        paquete.setTransportista(transportistaReal);
        paquete.setUltimaActualizacion(LocalDateTime.now());
        return paqueteRepository.save(paquete);
    }

    @Transactional
    public Paquete asignarUnidad(Long id, Long unidadId) {
        Paquete paquete = paqueteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paquete no encontrado con id: " + id));

        if (paquete.getEstado() == EstadoPaquete.ENTREGADO) {
            throw new IllegalStateException("No se puede asignar unidad a un paquete ya ENTREGADO.");
        }

        if (unidadId == null) {
            throw new IllegalArgumentException("Debes seleccionar una unidad activa.");
        }

        Unidad unidadReal = unidadRepository.findById(unidadId)
                .orElseThrow(() -> new IllegalArgumentException("Unidad no encontrada."));

        if (!unidadReal.isActiva()) {
            throw new IllegalStateException("No se puede asignar una unidad inactiva.");
        }

        paquete.setUnidad(unidadReal);
        paquete.setUltimaActualizacion(LocalDateTime.now());
        return paqueteRepository.save(paquete);
    }

    @Transactional
    public void eliminarPorId(Long id) {
        Paquete paquete = paqueteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paquete no encontrado con id: " + id));
        if (paquete.getEstado() == EstadoPaquete.EN_TRANSITO || paquete.getEstado() == EstadoPaquete.EN_REPARTO) {
            throw new IllegalStateException("No se puede eliminar un paquete que está en tránsito o en reparto.");
        }
        paqueteRepository.deleteById(id);
    }

    private void validarCamposRegistro(Paquete paquete) {
        if (esVacio(paquete.getRemitente()) || paquete.getRemitente().trim().length() < 3) {
            throw new IllegalArgumentException("El nombre del remitente es obligatorio (mínimo 3 caracteres).");
        }
        if (esVacio(paquete.getCiudadOrigen()) || paquete.getCiudadOrigen().trim().length() < 2) {
            throw new IllegalArgumentException("La ciudad o sucursal de origen es obligatoria.");
        }
        if (esVacio(paquete.getDestinatario()) || paquete.getDestinatario().trim().length() < 3) {
            throw new IllegalArgumentException("El nombre del destinatario es obligatorio (mínimo 3 caracteres).");
        }
        String telefono = paquete.getTelefonoDestinatario() == null
                ? "" : paquete.getTelefonoDestinatario().replaceAll("\\s+", "").trim();
        if (!telefono.matches("^[0-9]{10}$")) {
            throw new IllegalArgumentException("El teléfono del destinatario debe tener exactamente 10 dígitos numéricos.");
        }
        if (esVacio(paquete.getDireccionDestino()) || paquete.getDireccionDestino().trim().length() < 5) {
            throw new IllegalArgumentException("La dirección de destino es obligatoria (mínimo 5 caracteres).");
        }
        if (esVacio(paquete.getDescripcionContenido()) || paquete.getDescripcionContenido().trim().length() < 3) {
            throw new IllegalArgumentException("La descripción o tipo de contenido del paquete es obligatoria.");
        }
        if (paquete.getPesoKg() == null || paquete.getPesoKg() <= 0 || paquete.getPesoKg() > 500) {
            throw new IllegalArgumentException("El peso del paquete debe ser mayor a 0 kg y hasta 500 kg.");
        }
    }

    private String generarCodigoUnico() {
        String codigo;
        do {
            codigo = "DJP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (paqueteRepository.existsByCodigoRastreo(codigo));
        return codigo;
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}