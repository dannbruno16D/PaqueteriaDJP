package com.paqueteria.djp.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.paqueteria.djp.model.Paquete;
import com.paqueteria.djp.service.PaqueteService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final PaqueteService paqueteService;

    @GetMapping("/")
    public String inicio() {
        return "index";
    }

    @GetMapping("/rastreo")
    public String rastrear(@RequestParam(required = false) String codigo, Model model) {
        if (codigo != null && !codigo.isBlank()) {
            String codigoLimpio = codigo.trim();
            Paquete paquete = paqueteService.buscarPorCodigoRastreo(codigoLimpio).orElse(null);

            model.addAttribute("paqueteEncontrado", paquete);
            model.addAttribute("codigoBuscado", codigoLimpio);

            if (paquete != null && paquete.getHistorialEstados() != null) {
                List<Paquete.RegistroEstado> historialOrdenado = new ArrayList<>(paquete.getHistorialEstados());
                historialOrdenado.sort(
                        Comparator.comparing(
                                Paquete.RegistroEstado::getFechaHora,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        ).reversed()
                );
                model.addAttribute("historialOrdenado", historialOrdenado);
            }
        }
        return "rastreo";
    }
}
