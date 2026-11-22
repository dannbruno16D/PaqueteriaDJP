package com.paqueteria.djp.controller;

import java.util.ArrayList;
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
import com.paqueteria.djp.model.Unidad;
import com.paqueteria.djp.model.Usuario;
import com.paqueteria.djp.service.PaqueteService;
import com.paqueteria.djp.service.UnidadService;
import com.paqueteria.djp.service.UsuarioService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/almacen")
@RequiredArgsConstructor
public class AlmacenController {

    private final PaqueteService paqueteService;
    private final UsuarioService usuarioService;
    private final UnidadService unidadService;

    @GetMapping
    public String vistaAlmacen(@RequestParam(required = false) EstadoPaquete estadoFiltro, Model model) {
        List<Paquete> paquetesAlmacen;
        if (estadoFiltro != null) {
            paquetesAlmacen = paqueteService.listarPorEstado(estadoFiltro);
        } else {
            paquetesAlmacen = new ArrayList<>();
            paquetesAlmacen.addAll(paqueteService.listarPorEstado(EstadoPaquete.EN_ALMACEN));
            paquetesAlmacen.addAll(paqueteService.listarPorEstado(EstadoPaquete.REGISTRADO));
            paquetesAlmacen.addAll(paqueteService.listarPorEstado(EstadoPaquete.INCIDENCIA));
        }

        model.addAttribute("paquetes", paquetesAlmacen);
        model.addAttribute("estadoFiltro", estadoFiltro);
        model.addAttribute("estados", EstadoPaquete.values());
        model.addAttribute("transportistas", usuarioService.listarTransportistasActivos());
        model.addAttribute("unidades", unidadService.listarUnidadesActivas());
        return "almacen";
    }

    @PostMapping("/{id}/ingresar")
    public String ingresarAAlmacen(@PathVariable Long id,
                                   @RequestParam(required = false) String ubicacion,
                                   @RequestParam(required = false) String detalle,
                                   RedirectAttributes redirectAttributes) {
        try {
            String ubicacionFinal = (ubicacion == null || ubicacion.isBlank())
                    ? "Almacén Central DJP"
                    : ubicacion.trim();
            String detalleFinal = (detalle == null || detalle.isBlank())
                    ? "Paquete recibido y verificado en almacén."
                    : detalle.trim();

            Paquete actualizado = paqueteService.actualizarEstado(
                    id, EstadoPaquete.EN_ALMACEN, ubicacionFinal, detalleFinal
            );
            redirectAttributes.addFlashAttribute(
                    "mensaje", "Paquete " + actualizado.getCodigoRastreo() + " ingresado a Almacén."
            );
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/almacen";
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstadoDesdeAlmacen(@PathVariable Long id,
                                          @RequestParam EstadoPaquete estado,
                                          @RequestParam(required = false) String ubicacion,
                                          @RequestParam(required = false) String detalle,
                                          RedirectAttributes redirectAttributes) {
        try {
            Paquete actualizado = paqueteService.actualizarEstado(id, estado, ubicacion, detalle);
            redirectAttributes.addFlashAttribute(
                    "mensaje", "Estado de " + actualizado.getCodigoRastreo() + " actualizado a " + estado.getEtiqueta() + "."
            );
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/almacen";
    }

    @PostMapping("/{id}/unidad")
    public String asignarUnidadDesdeAlmacen(@PathVariable Long id,
                                          @RequestParam(required = false) Long unidadId,
                                          RedirectAttributes redirectAttributes) {
        try {
            if (unidadId == null) {
                redirectAttributes.addFlashAttribute("error", "Selecciona una unidad activa.");
                return "redirect:/almacen";
            }
            Paquete actualizado = paqueteService.asignarUnidad(id, unidadId);
            Unidad unidadAsignada = actualizado.getUnidad();
            redirectAttributes.addFlashAttribute(
                    "mensaje", "Unidad " + unidadAsignada.getCodigoUnidad() + " (" + unidadAsignada.getDescripcion()
                            + ") asignada a " + actualizado.getCodigoRastreo() + "."
            );
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/almacen";
    }

    @PostMapping("/{id}/transportista")
    public String asignarTransportistaDesdeAlmacen(@PathVariable Long id,
                                                @RequestParam(required = false) Long transportistaId,
                                                RedirectAttributes redirectAttributes) {
        try {
            if (transportistaId == null) {
                redirectAttributes.addFlashAttribute("error", "Selecciona un transportista activo.");
                return "redirect:/almacen";
            }
            Usuario transportista = usuarioService.buscarPorId(transportistaId)
                    .orElseThrow(() -> new IllegalArgumentException("Transportista no encontrado: " + transportistaId));
            Paquete actualizado = paqueteService.asignarTransportista(id, transportista);
            redirectAttributes.addFlashAttribute(
                    "mensaje", "Transportista " + actualizado.getTransportista().getNombre()
                            + " asignado a " + actualizado.getCodigoRastreo() + "."
            );
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/almacen";
    }
}