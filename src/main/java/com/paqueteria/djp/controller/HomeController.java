package com.paqueteria.djp.controller;

import com.paqueteria.djp.service.PaqueteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
            model.addAttribute("paqueteEncontrado",
                    paqueteService.buscarPorCodigoRastreo(codigo.trim()).orElse(null));
            model.addAttribute("codigoBuscado", codigo.trim());
        }
        return "rastreo";
    }
}
