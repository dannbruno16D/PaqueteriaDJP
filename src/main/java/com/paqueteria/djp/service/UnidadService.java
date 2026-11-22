package com.paqueteria.djp.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paqueteria.djp.model.Unidad;
import com.paqueteria.djp.repository.UnidadRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UnidadService {

    private final UnidadRepository unidadRepository;

    public List<Unidad> listarTodas() {
        return unidadRepository.findAll();
    }

    public List<Unidad> listarUnidadesActivas() {
        return unidadRepository.findByActivaTrueOrderByCodigoUnidadAsc();
    }

    public Optional<Unidad> buscarPorId(Long id) {
        return unidadRepository.findById(id);
    }

    @Transactional
    public Unidad guardar(Unidad unidad) {
        if (unidad == null || unidad.getCodigoUnidad() == null || unidad.getCodigoUnidad().isBlank()) {
            throw new IllegalArgumentException("El código de la unidad es obligatorio.");
        }
        return unidadRepository.save(unidad);
    }
}
