package com.paqueteria.djp.controller;

import com.paqueteria.djp.model.EstadoPaquete;
import com.paqueteria.djp.model.Paquete;
import com.paqueteria.djp.model.Rol;
import com.paqueteria.djp.model.Usuario;
import com.paqueteria.djp.service.PaqueteService;
import com.paqueteria.djp.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/paquetes")
@RequiredArgsConstructor
public class PaqueteController {

    private final PaqueteService paqueteService;
    private final UsuarioService usuarioService;

    @GetMapping
    public String listarPaquetes(@RequestParam(required = false) String codigoRastreo, Model model) {
        if (codigoRastreo != null && !codigoRastreo.isBlank()) {
            model.addAttribute("paquetes",
                    paqueteService.buscarPorCodigoRastreo(codigoRastreo.trim())
                            .map(java.util.List::of)
                            .orElseGet(java.util.List::of));
            model.addAttribute("codigoBuscado", codigoRastreo.trim());
        } else {
            model.addAttribute("paquetes", paqueteService.listarTodos());
        }

        model.addAttribute("nuevoPaquete", new Paquete());
        model.addAttribute("estados", EstadoPaquete.values());
        model.addAttribute("transportistas", usuarioService.listarPorRol(Rol.TRANSPORTISTA));
        return "paquetes";
    }

    @PostMapping("/registrar")
    public String registrarPaquete(@ModelAttribute("nuevoPaquete") Paquete paquete,
                                   RedirectAttributes redirectAttributes) {
        Paquete guardado = paqueteService.registrarPaquete(paquete);
        redirectAttributes.addFlashAttribute("mensaje",
                "Paquete registrado con código: " + guardado.getCodigoRastreo());
        return "redirect:/paquetes";
    }

    @PostMapping("/{id}/estado")
    public String actualizarEstado(@PathVariable Long id,
                                   @RequestParam EstadoPaquete estado,
                                   RedirectAttributes redirectAttributes) {
        paqueteService.actualizarEstado(id, estado);
        redirectAttributes.addFlashAttribute("mensaje", "Estado actualizado a " + estado);
        return "redirect:/paquetes";
    }

    @PostMapping("/{id}/transportista")
    public String asignarTransportista(@PathVariable Long id,
                                       @RequestParam Long transportistaId,
                                       RedirectAttributes redirectAttributes) {
        Usuario transportista = usuarioService.buscarPorId(transportistaId)
                .orElseThrow(() -> new IllegalArgumentException("Transportista no encontrado: " + transportistaId));
        paqueteService.asignarTransportista(id, transportista);
        redirectAttributes.addFlashAttribute("mensaje", "Transportista asignado correctamente");
        return "redirect:/paquetes";
    }
}
