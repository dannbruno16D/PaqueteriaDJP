package com.paqueteria.djp.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/transportistas")
@RequiredArgsConstructor
public class TransportistaController {

    private final PaqueteService paqueteService;
    private final UsuarioService usuarioService;

    @GetMapping
    public String vistaTransportistas(@RequestParam(required = false) Long transportistaId, Model model) {
        List<Usuario> transportistas = usuarioService.listarTransportistasActivos();
        Usuario seleccionado = null;

        if (transportistaId != null) {
            seleccionado = transportistas.stream()
                    .filter(u -> u.getId().equals(transportistaId))
                    .findFirst()
                    .orElse(null);
        } else if (!transportistas.isEmpty()) {
            seleccionado = transportistas.get(0);
        }

        List<Paquete> asignados = (seleccionado != null)
                ? paqueteService.listarPorTransportistaId(seleccionado.getId())
                : List.of();

        model.addAttribute("transportistas", transportistas);
        model.addAttribute("transportistaSeleccionado", seleccionado);
        model.addAttribute("paquetes", asignados);
        model.addAttribute("estados", EstadoPaquete.values());
        return "transportistas";
    }

    @PostMapping("/paquetes/{id}/estado")
    public String actualizarEstadoRuta(@PathVariable Long id,
                                     @RequestParam Long transportistaId,
                                     @RequestParam EstadoPaquete estado,
                                     @RequestParam(required = false) String ubicacion,
                                     @RequestParam(required = false) String detalle,
                                     RedirectAttributes redirectAttributes) {
        try {
            Paquete actualizado = paqueteService.actualizarEstado(id, estado, ubicacion, detalle);
            redirectAttributes.addFlashAttribute(
                    "mensaje", "Envío " + actualizado.getCodigoRastreo() + " actualizado a " + estado.getEtiqueta() + "."
            );
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/transportistas?transportistaId=" + transportistaId;
    }
}
