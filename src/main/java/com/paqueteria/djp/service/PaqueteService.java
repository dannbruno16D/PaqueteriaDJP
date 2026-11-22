package com.paqueteria.djp.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paqueteria.djp.model.EstadoPaquete;
import com.paqueteria.djp.model.Paquete;
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

    private final PaqueteRepository paqueteRepository;
    private final UnidadRepository unidadRepository;
    private final UsuarioRepository usuarioRepository;

    public List<Paquete> listarTodos() {
        return paqueteRepository.findAll();
    }

    public Optional<Paquete> buscarPorId(Long id) {
        return paqueteRepository.findById(id);
    }

    public Optional<Paquete> buscarPorCodigoRastreo(String codigoRastreo) {
        return paqueteRepository.findByCodigoRastreo(codigoRastreo);
    }

    public List<Paquete> listarPorEstado(EstadoPaquete estado) {
        return paqueteRepository.findByEstado(estado);
    }

    public List<Paquete> listarPorTransportista(Usuario transportista) {
        return paqueteRepository.findByTransportista(transportista);
    }

    public List<Paquete> listarPorTransportistaId(Long transportistaId) {
        Usuario transportista = usuarioRepository.findById(transportistaId)
                .orElseThrow(() -> new IllegalArgumentException("Transportista no encontrado con id: " + transportistaId));
        return paqueteRepository.findByTransportista(transportista);
    }

    @Transactional
    public Paquete registrarPaquete(Paquete paquete) {
        if (paquete.getCodigoRastreo() == null || paquete.getCodigoRastreo().isBlank()) {
            String codigoGenerado = "DJP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            paquete.setCodigoRastreo(codigoGenerado);
        }
        if (paquete.getEstado() == null) {
            paquete.setEstado(EstadoPaquete.REGISTRADO);
        }
        return paqueteRepository.save(paquete);
    }

    @Transactional
    public Paquete actualizarEstado(Long id, EstadoPaquete nuevoEstado) {
        Paquete paquete = paqueteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paquete no encontrado con id: " + id));
        paquete.setEstado(nuevoEstado);
        return paqueteRepository.save(paquete);
    }

    @Transactional
    public Paquete actualizarEstado(Long id, EstadoPaquete nuevoEstado, String ubicacion, String detalle) {
        Paquete paquete = paqueteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paquete no encontrado con id: " + id));
        paquete.setEstado(nuevoEstado);
        return paqueteRepository.save(paquete);
    }

    @Transactional
    public Paquete asignarUnidad(Long id, Long unidadId) {
        Paquete paquete = paqueteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paquete no encontrado con id: " + id));
        Unidad unidad = unidadRepository.findById(unidadId)
                .orElseThrow(() -> new IllegalArgumentException("Unidad no encontrada con id: " + unidadId));
        paquete.setUnidad(unidad);
        return paqueteRepository.save(paquete);
    }

    @Transactional
    public Paquete asignarTransportista(Long id, Usuario transportista) {
        Paquete paquete = paqueteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paquete no encontrado con id: " + id));
        paquete.setTransportista(transportista);
        return paqueteRepository.save(paquete);
    }

    @Transactional
    public void eliminarPorId(Long id) {
        paqueteRepository.deleteById(id);
    }
}
