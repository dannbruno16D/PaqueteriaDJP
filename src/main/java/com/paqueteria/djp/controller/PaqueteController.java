package com.paqueteria.djp.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.paqueteria.djp.model.EstadoPaquete;
import com.paqueteria.djp.model.Paquete;
import com.paqueteria.djp.model.Usuario;
import com.paqueteria.djp.service.PaqueteService;
import com.paqueteria.djp.service.UsuarioService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/paquetes")
@RequiredArgsConstructor
public class PaqueteController {

    private final PaqueteService paqueteService;
    private final UsuarioService usuarioService;

    @GetMapping
    public String listarPaquetes(@RequestParam(required = false) String codigoRastreo, Model model) {
        if (codigoRastreo != null && !codigoRastreo.isBlank()) {
            String codigoLimpio = codigoRastreo.trim();
            model.addAttribute("paquetes",
                    paqueteService.buscarPorCodigoRastreo(codigoLimpio)
                            .map(List::of)
                            .orElseGet(List::of));
            model.addAttribute("codigoBuscado", codigoLimpio);
        } else {
            model.addAttribute("paquetes", paqueteService.listarTodos());
        }

        if (!model.containsAttribute("nuevoPaquete")) {
            model.addAttribute("nuevoPaquete", new Paquete());
        }
        model.addAttribute("estados", EstadoPaquete.values());
        model.addAttribute("transportistas", usuarioService.listarTransportistasActivos());
        return "paquetes";
    }

    @PostMapping("/registrar")
    public String registrarPaquete(@ModelAttribute("nuevoPaquete") Paquete paquete,
                                   RedirectAttributes redirectAttributes) {
        try {
            Paquete guardado = paqueteService.registrarPaquete(paquete);
            redirectAttributes.addFlashAttribute("mensaje",
                    "Paquete registrado con código: " + guardado.getCodigoRastreo());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            redirectAttributes.addFlashAttribute("nuevoPaquete", paquete);
        }
        return "redirect:/paquetes";
    }

    @PostMapping("/{id}/estado")
    public String actualizarEstado(@PathVariable Long id,
                                   @RequestParam EstadoPaquete estado,
                                   @RequestParam(required = false) String ubicacion,
                                   @RequestParam(required = false) String detalle,
                                   RedirectAttributes redirectAttributes) {
        try {
            Paquete actualizado = paqueteService.actualizarEstado(id, estado, ubicacion, detalle);
            redirectAttributes.addFlashAttribute("mensaje",
                    "Estado de " + actualizado.getCodigoRastreo() + " actualizado a " + estado.getEtiqueta() + ".");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/paquetes";
    }

    @PostMapping("/{id}/transportista")
    public String asignarTransportista(@PathVariable Long id,
                                       @RequestParam(required = false) Long transportistaId,
                                       RedirectAttributes redirectAttributes) {
        try {
            if (transportistaId == null) {
                redirectAttributes.addFlashAttribute("error", "Selecciona un transportista antes de asignar.");
                return "redirect:/paquetes";
            }
            Usuario transportista = usuarioService.buscarPorId(transportistaId)
                    .orElseThrow(() -> new IllegalArgumentException("Transportista no encontrado: " + transportistaId));
            Paquete actualizado = paqueteService.asignarTransportista(id, transportista);
            redirectAttributes.addFlashAttribute("mensaje",
                    "Transportista " + actualizado.getTransportista().getNombre()
                            + " asignado a " + actualizado.getCodigoRastreo() + ".");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/paquetes";
    }
}