package com.paqueteria.djp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.paqueteria.djp.model.Rol;
import com.paqueteria.djp.model.Usuario;
import com.paqueteria.djp.service.UsuarioService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        if (!model.containsAttribute("nuevoUsuario")) {
            Usuario nuevo = new Usuario();
            nuevo.setActivo(true);
            model.addAttribute("nuevoUsuario", nuevo);
        }
        model.addAttribute("roles", Rol.values());
        return "usuarios";
    }

    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute("nuevoUsuario") Usuario usuario,
                                 RedirectAttributes redirectAttributes) {
        try {
            Usuario guardado = usuarioService.guardar(usuario);
            redirectAttributes.addFlashAttribute(
                    "mensaje", "Usuario @" + guardado.getUsername() + " guardado correctamente."
            );
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            redirectAttributes.addFlashAttribute("nuevoUsuario", usuario);
        }
        return "redirect:/usuarios";
    }

    @PostMapping("/{id}/activo")
    public String cambiarActivo(@PathVariable Long id,
                                @RequestParam boolean activo,
                                RedirectAttributes redirectAttributes) {
        try {
            Usuario actualizado = usuarioService.cambiarEstadoActivo(id, activo);
            String estadoTexto = actualizado.isActivo() ? "activado" : "desactivado";
            redirectAttributes.addFlashAttribute(
                    "mensaje", "Usuario @" + actualizado.getUsername() + " " + estadoTexto + "."
            );
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/usuarios";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.eliminarPorId(id);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado correctamente.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/usuarios";
    }
}